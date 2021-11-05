package com.xforceplus.ultraman.oqsengine.storage;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.pojo.page.PageScope;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.define.OperationType;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.accumulator.TransactionAccumulator;
import com.xforceplus.ultraman.oqsengine.storage.transaction.commit.CommitHelper;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 联合搜索.主要执行如下逻辑. 1. 从unSyncStorage中搜索未同步的数据,预期数据量会较少. 2. 根据第一步的结果构造过滤列表,在syncedStorage中搜索. 3. 合并两者查询结果.
 *
 * @author dongbin
 * @version 0.1 2021/10/14 17:50
 * @since 1.8
 */
public class CombinedSelectStorage implements ConditionsSelectStorage {

    private Logger logger = LoggerFactory.getLogger(CombinedSelectStorage.class);

    private ConditionsSelectStorage unSyncStorage;

    private ConditionsSelectStorage syncedStorage;

    private TransactionManager transactionManager;

    private CommitIdStatusService commitIdStatusService;

    private Function<Sort[], Comparator<EntityRef>> comparatorSupplier;

    /**
     * 构造一个联合查询.
     *
     * @param unSyncStorage 未同步实例查询.
     * @param syncedStorage 已同步实例查询.
     */
    public CombinedSelectStorage(
        ConditionsSelectStorage unSyncStorage,
        ConditionsSelectStorage syncedStorage) {
        this.unSyncStorage = unSyncStorage;
        this.syncedStorage = syncedStorage;

        // 比较器构建.
        comparatorSupplier = (sorts) -> {
            Comparator<EntityRef> comparator = null;

            for (int i = 0; i < sorts.length; i++) {
                Sort sort = sorts[i];
                int finalI = i;
                Comparator<EntityRef> c =
                    Comparator.comparing(r -> r.getSortValue(finalI).orElseGet(null), (v1, v2) -> {
                        FieldType type = sort.getField().type();
                        return type.compareFromStringValue(v1, v2);
                    });
                if (sort.isDes()) {
                    c = c.reversed();
                }

                if (comparator == null) {
                    comparator = c;
                } else {
                    comparator.thenComparing(c);
                }
            }

            return comparator;
        };
    }

    public void setTransactionManager(
        TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void setCommitIdStatusService(CommitIdStatusService commitIdStatusService) {
        this.commitIdStatusService = commitIdStatusService;
    }

    /**
     * 联合查询.
     *
     * @param conditions  查询条件.
     * @param entityClass 目标元信息.
     * @param config      查询配置.
     * @return 查询结果.
     * @throws SQLException 查询异常.
     */
    @Override
    public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, SelectConfig config)
        throws SQLException {
        Collection<EntityRef> masterRefs = Collections.emptyList();

        long commitId = checkCommitId(config);
        Sort sort = config.getSort();
        Sort secondSort = config.getSecondarySort();
        Sort thirdSort = config.getThirdSort();
        Sort[] sorts = buildSorts(config);
        Page page = config.getPage();
        Conditions filterCondition = config.getDataAccessFilterCondtitions();

        if (commitId > 0) {
            //trigger master search
            masterRefs = unSyncStorage.select(
                conditions,
                entityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withSort(sort)
                    .withSecondarySort(secondSort)
                    .withThirdSort(thirdSort)
                    .withCommitId(commitId)
                    .withDataAccessFitlerCondtitons(filterCondition)
                    .build());
        }

        for (EntityRef ref : masterRefs) {
            if (ref.getOp() == OperationType.UNKNOWN.getValue()) {
                throw new SQLException(String.format("Expected operation type unknown.[id=%d]", ref.getId()));
            }
        }

        masterRefs = fixNullSortValue(masterRefs, sorts);

        /*
         * filter ids
         */
        Set<Long> filterIdsFromMaster = masterRefs.stream()
            .filter(
                x -> x.getOp() == OperationType.DELETE.getValue() || x.getOp() == OperationType.UPDATE.getValue())
            .map(EntityRef::getId)
            .collect(toSet());


        /*
         * 这里在查询索引时新创建一个page的原因是在查询索引时会调用page.getNextPage()造成当前页增加.相当于如下.
         * Page page = new Page(1,20);
         * page.getNextPage();
         * page.getNextPage();
         *
         * 第二次的调用会造成错误的读取后一页.
         * 为了让getNextPage()方法一个Page实例只能调用一次.
         */
        Page indexPage;
        try {
            indexPage = page.clone();
        } catch (CloneNotSupportedException e) {
            throw new SQLException(e.getMessage(), e);
        }

        Collection<EntityRef> indexRefs = syncedStorage.select(
            conditions,
            entityClass,
            SelectConfig.Builder.anSelectConfig()
                .withSort(sort)
                .withSecondarySort(secondSort)
                .withThirdSort(thirdSort)
                .withPage(indexPage)
                .withExcludedIds(filterIdsFromMaster)
                .withDataAccessFitlerCondtitons(filterCondition)
                .withCommitId(commitId).build()
        );
        indexRefs = fixNullSortValue(indexRefs, sorts);

        Collection<EntityRef> masterRefsWithoutDeleted = masterRefs.stream()
            .filter(x -> x.getOp() != OperationType.DELETE.getValue()).collect(toList());

        page.setTotalCount(indexPage.getTotalCount() + masterRefsWithoutDeleted.size());
        if (page.isEmptyPage()) {
            return Collections.emptyList();
        }

        if (!page.hasNextPage()) {
            return Collections.emptyList();
        }

        PageScope scope = page.getNextPage();
        long pageSize = page.getPageSize();

        long skips = scope == null ? 0 : scope.getStartLine();
        return mergeToStream(masterRefsWithoutDeleted, indexRefs, sorts)
            .skip(skips < 0 ? 0 : skips).limit(pageSize).collect(toList());
    }

    // 根据排序设定情况,返回一个数组包含了所有的排序字段,大小有可能是0到3不定.
    private Sort[] buildSorts(SelectConfig config) {
        if (config.getSort().isOutOfOrder()) {
            return new Sort[0];
        }

        // 最大处理的排序字段数量.
        final int maxSortField = 3;
        // 如果当前的排序字段为非排序,那么此排序之后的所有排序都为非排序.
        return IntStream.range(0, maxSortField).mapToObj(i -> {
            switch (i) {
                case 0:
                    return config.getSort();
                case 1: {
                    if (config.getSort().isOutOfOrder()) {
                        return Sort.buildOutOfSort();
                    } else {
                        return config.getSecondarySort();
                    }
                }
                case 2: {
                    if (config.getThirdSort().isOutOfOrder()) {
                        return Sort.buildOutOfSort();
                    } else {
                        return config.getThirdSort();
                    }
                }
                default:
                    return Sort.buildOutOfSort();
            }
        }).filter(s -> !s.isOutOfOrder()).toArray(Sort[]::new);
    }

    private Stream<EntityRef> mergeToStream(
        Collection<EntityRef> masterRefs, Collection<EntityRef> indexRefs,
        Sort[] sorts) {
        Stream<EntityRef> refStream = Stream.concat(masterRefs.stream(), indexRefs.stream());
        if (sorts.length == 0) {
            return refStream;
        }

        Comparator<EntityRef> refComparator = comparatorSupplier.apply(sorts);

        return refStream.sorted(refComparator);
    }

    // 如果排序,但是查询结果没有值.
    private Collection<EntityRef> fixNullSortValue(Collection<EntityRef> refs, Sort[] sorts) {
        int sortIndex = 0;
        for (Sort sort : sorts) {
            if (!sort.isOutOfOrder()) {
                for (EntityRef r : refs) {
                    if (r.getOrderValue() == null || r.getOrderValue().isEmpty()) {
                        if (sort.getField().config().isIdentifie()) {
                            setSortValue(sortIndex, r, Long.toString(r.getId()));
                        } else {
                            setSortValue(sortIndex, r, sort.getField().type().getDefaultSortValue());
                        }
                    }
                }

                sortIndex++;

            } else {
                break;
            }
        }

        return refs;
    }

    private void setSortValue(int sortIndex, EntityRef ref, String value) {
        // 如果是意外的字段,那么设置为一个字符串0,数字和字符串都可以正常转型.
        final String finalValue = "0";
        ref.setSortValue(sortIndex, value);
        if (ref.getSortValue(sortIndex).isPresent()) {
            ref.setSortValue(sortIndex, finalValue);
        }
    }

    private long checkCommitId(SelectConfig config) {
        if (config.getCommitId() > 0) {
            return config.getCommitId();
        }

        long minUnSyncCommitId = 0;
        if (commitIdStatusService != null) {
            // 获取提交号.
            Optional<Long> minUnSyncCommitIdOp = commitIdStatusService.getMin();
            if (!minUnSyncCommitIdOp.isPresent()) {
                minUnSyncCommitId = 0;
                if (logger.isDebugEnabled()) {
                    logger.debug("Unable to fetch the commit number, use the default commit number 0.");
                }
            } else {
                minUnSyncCommitId = minUnSyncCommitIdOp.get();
                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "The minimum commit number {} that is currently uncommitted was successfully obtained.",
                        minUnSyncCommitId);
                }
            }
        }

        /*
         * 校正查询提交号,防止由于当前事务中未提交但是无法查询到这些数据的问题.
         * 未提交的数据的提交号都标示为 CommitHelper.getUncommitId() 的返回值. 这里需要修正以下情况的查询.
         * 1.在事务中并且未提交.
         * 2.之前有过写入动作.
         */
        if (transactionManager != null && minUnSyncCommitId <= 0) {
            Optional<Transaction> currentTransaction = transactionManager.getCurrent();
            if (currentTransaction.isPresent()) {
                Transaction transaction = currentTransaction.get();
                TransactionAccumulator accumulator = transaction.getAccumulator();
                // 没有写和的操作序号值.
                final int noWriteOpSize = 0;
                if (accumulator.getBuildNumbers()
                    + accumulator.getReplaceNumbers()
                    + accumulator.getDeleteNumbers() > noWriteOpSize) {
                    minUnSyncCommitId = CommitHelper.getUncommitId();
                }
            }
        }

        return minUnSyncCommitId;
    }
}
