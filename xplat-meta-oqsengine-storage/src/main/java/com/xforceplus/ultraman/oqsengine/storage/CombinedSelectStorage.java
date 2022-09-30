package com.xforceplus.ultraman.oqsengine.storage;

import static java.util.stream.Collectors.toList;

import com.xforceplus.ultraman.oqsengine.common.debug.Debug;
import com.xforceplus.ultraman.oqsengine.pojo.define.OperationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.pojo.page.PageScope;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.accumulator.TransactionAccumulator;
import com.xforceplus.ultraman.oqsengine.storage.transaction.commit.CommitHelper;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 联合查询,合并主库和索引两次查询的结果过滤重排序得并载取得到最终结果.
 * 查询步骤如下.
 * <ol>
 *     <li>基于当前分页大小计算一个"溢出"比例,最终索引的查询页大小为 页大小 + (页大小) * 溢出比例.</li>
 *     <li>获取当前最小提交号.</li>
 *     <li>使用新的溢出分页信息查询索引.</li>
 *     <li>查询主库.</li>
 *     <li>其于主库查询结果在索引中合并,去重,去除删除的实例.</li>
 *     <li>检查当前查询结果是否满足要求页大小,如果不满足且还有对象没有被读取计算新的溢出比例回到第1步重新开始.</li>
 * </ol>
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

    /*
    查询溢出使用的比例.
     */
    private static final float[] OVERFLOW_RATIO = new float[] {
        0.1F, 0.3F, 0.5F, 0.8F, 1.0F
    };

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

        Collection<EntityRef> refs;
        for (int i = 0; i < OVERFLOW_RATIO.length; i++) {
            refs = doSelect(conditions, entityClass, config, OVERFLOW_RATIO[i]);

            if (refs != null) {
                return refs;
            } else {
                /*
                 查询溢出失败,等待 waitDuration 毫秒后使用更大的溢出比例重试.
                 */
                final long waitDuration = 500;
                if (logger.isWarnEnabled()) {
                    logger.warn("Overflow failed during query. Wait for {} milliseconds and try again.", waitDuration);
                }

                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(waitDuration));
            }
        }

        // 尝试了所有的溢出比例,仍然无法得到满足页数的结果.
        throw new SQLException("Select overflow failed.");
    }

    /*
    返回值如果是null,表示需要当前溢出比例不足够.
     */
    private Collection<EntityRef> doSelect(
        Conditions conditions, IEntityClass entityClass, SelectConfig config, float overflowRatio) throws SQLException {
        Sort sort = config.getSort();
        Sort secondSort = config.getSecondarySort();
        Sort thirdSort = config.getThirdSort();
        Page page = config.getPage();
        Conditions filterCondition = config.getDataAccessFilterCondtitions();

        // 索引查询使用Page,防止修改传入的Page.
        Page indexPage = createIndexPage(page, overflowRatio);

        /*
        此处必须保证一定有一个不为0的提交号.
         */
        long commitId = this.buildQueryCommitId(config);

        /*
        分别从索引和主库中条件查询,最终合并两者的结果并载取出最终的目标结果.
        步骤如下.
        1. 获取当前未同步提交号.
        2. 索引中查询小于当前提交号的数据,因为索引manticore对于更新是非原子的,所以需要小于指定提交号保证查询的目标是安全的.
        3. 使用大于等于提交号查询主库,不查询已经被删除的数据.
        4. 合并两个查询结果,并重新按照排序要求排序并载取目标长度.

        注意: 由于是两次查询,在两次查询之间数据是可能会改变的.
          1. 两个查询之间数据被更新.从不符合条件转为符合条件或者相反,那么此实例会最终放入结果集中.
          2. 两个查询之间数据被删除.索引符合条件,主库不符合,那么结果将放入结果集中.反之不会放入.

          由于提交号的划分,这里设定相同的对象要么出现在索引结果中,要么出现在主库结果中.
         */
        Collection<EntityRef> masterRefs;
        Collection<EntityRef> indexRefs;
        SelectConfig indexSelectConfig = SelectConfig.Builder.anSelectConfig()
            .withSort(sort)
            .withSecondarySort(secondSort)
            .withThirdSort(thirdSort)
            .withPage(indexPage)
            .withDataAccessFitlerCondtitons(filterCondition)
            .withCommitId(commitId).build();
        indexRefs = syncedStorage.select(conditions, entityClass, indexSelectConfig);

        /*
        此为模似两次查询之间的间隔,由外部调用控制设定.
         */
        if (logger.isDebugEnabled()) {
            if (Debug.needMasterAndIndexSelectWait()) {
                logger.debug("It is found that Debug needs to block after the master query ends.");
            }
        }
        Debug.awaitNoticeMasterAndIndexSelect();

        if (commitId > 0) {
            SelectConfig masterSelectConfig = SelectConfig.Builder.anSelectConfig()
                .withSort(sort)
                .withSecondarySort(secondSort)
                .withThirdSort(thirdSort)
                .withCommitId(commitId)
                .withDataAccessFitlerCondtitons(filterCondition)
                .build();
            masterRefs = unSyncStorage.select(conditions, entityClass, masterSelectConfig);
        } else {
            masterRefs = Collections.emptyList();
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                "The query condition of the union is ({}), the commitId is {} and the master result is {} and index result is {}.",
                conditions.toString(),
                commitId,
                Arrays.toString(masterRefs.stream().mapToLong(r -> r.getId()).toArray()),
                Arrays.toString(indexRefs.stream().mapToLong(r -> r.getId()).toArray())
            );
        }

        // 记录索引未过滤前的数量.
        int indexOriginalSize = indexRefs.size();
        // 记录过滤之前的数据,包含创建,更新和删除.
        int masterOriginalSize = masterRefs.size();
        /*
        以主库为标准,去除索引中所有 EntityRef.getId() 相同的实例.
        再去除主库中 EntityRef.getOp() == OperationType.DELETE 的值.
        最终结果是索引中只包含主库中查询不到的结果.
         */
        // 操作会影响最终数据总量.
        int masterDeleteSize = 0;
        // 记录从索引结果中移除的更新实例数量.
        AtomicInteger removeUpdateRefFormIndexSize = new AtomicInteger();
        if (!masterRefs.isEmpty()) {
            // 分类统计数量.
            for (EntityRef ref : masterRefs) {
                if (OperationType.DELETE.getValue() == ref.getOp()) {
                    masterDeleteSize++;
                }
            }

            if (!indexRefs.isEmpty()) {
                // 主库ref速查表.
                Map<Long, EntityRef> masterRefTable =
                    masterRefs.stream().collect(Collectors.toMap(r -> r.getId(), r -> r, (r0, r1) -> r0));

                /*
                op 状态只有主库查询结果中有效,索引中不含有OP操作.
                所以此处判断EntityRef状态仍需要从主库结果中判断.
                 */
                indexRefs = indexRefs.stream().filter(r -> {
                    EntityRef masterRef = masterRefTable.get(r.getId());
                    if (masterRef != null) {
                        if (OperationType.UPDATE.getValue() == masterRef.getOp()) {
                            // 记录从索引结果中移除的在主库结果中出现的数量.
                            removeUpdateRefFormIndexSize.incrementAndGet();
                        }
                        return false;
                    } else {
                        return true;
                    }
                }).collect(toList());
            }
            // 主库存中去除被删除的实例.
            masterRefs =
                masterRefs.stream().filter(r -> r.getOp() != OperationType.DELETE.getValue()).collect(toList());
        }

        /*
        如果过滤后数据量不够当前页,检查是否有更多数据.
        查看索引是否还有更多页数据,如果有即进行下次查询.
        否则表示数据只有这么多,正常返回.
         */
        int currentSize = masterRefs.size() + indexRefs.size();
        if (currentSize < page.getPageSize()) {
            if (indexPage.getTotalCount() > indexOriginalSize) {
                return null;
            }
        }

        /*
        总数计算 = 索引总量 - 主库更新从索引中移除量 + 主库查询原始数据(包含创建更新和删除) - 主库删除数量.
        主库存更新从索引中移除量意义为,根据当前查询的主库结果中过滤出操作为更新的实例减去索引中也含有的数量.
        此是为了保证,同样一个实例在更新时有可能出现在索引中也可能不出现.
         */
        long totalSize =
            indexPage.getTotalCount()
                - removeUpdateRefFormIndexSize.get() + masterOriginalSize - masterDeleteSize;
        page.setTotalCount(totalSize < 0 ? 0 : totalSize);
        if (page.isEmptyPage() || !page.hasNextPage()) {
            return Collections.emptyList();
        }

        // 排序.修正可能的空值,以方便排序.
        Sort[] sorts = buildSorts(config);
        if (!indexRefs.isEmpty()) {
            fixNullSortValue(indexRefs, sorts);
        }
        if (!masterRefs.isEmpty()) {
            fixNullSortValue(masterRefs, sorts);
        }

        // 合并索引和主库(包含创建,更新和删除的所有对象)
        Stream<EntityRef> combinedRefStream = Stream.concat(indexRefs.stream(), masterRefs.stream());
        combinedRefStream = sort(combinedRefStream, sorts);

        PageScope scope = page.getNextPage();
        long pageSize = page.getPageSize();
        long skips = scope == null ? 0 : scope.getStartLine();
        skips = skips < 0 ? 0 : skips;
        Collection<EntityRef> combinedRefs = combinedRefStream.skip(skips).limit(pageSize).collect(toList());
        return combinedRefs;
    }

    private Stream<EntityRef> sort(Stream<EntityRef> refStream, Sort[] sorts) {
        if (sorts.length == 0) {
            return refStream;
        }

        final int firstSortIndex = 0;
        final int secondSortIndex = 1;
        final int thridSortIndex = 2;

        final int hasFirstSortLen = 1;
        final int hasSecondSortLen = 2;
        final int hasThridSortLen = 3;

        if (sorts.length == hasThridSortLen) {
            //三字段联排.
            return refStream.sorted(
                Comparator.comparing(
                    EntityRef::getOrderValue,
                    sorts[firstSortIndex].isAsc()
                        ? new SortValueComparator(sorts[firstSortIndex])
                        : new SortValueComparator(sorts[firstSortIndex]).reversed()
                ).thenComparing(
                    EntityRef::getSecondOrderValue,
                    sorts[secondSortIndex].isAsc()
                        ? new SortValueComparator(sorts[secondSortIndex])
                        : new SortValueComparator(sorts[secondSortIndex]).reversed()
                ).thenComparing(
                    EntityRef::getThridOrderValue,
                    sorts[thridSortIndex].isAsc()
                        ? new SortValueComparator(sorts[thridSortIndex])
                        : new SortValueComparator(sorts[thridSortIndex]).reversed()
                )
            );
        } else if (sorts.length == hasSecondSortLen) {
            //二字段联排.
            return refStream.sorted(
                Comparator.comparing(
                    EntityRef::getOrderValue,
                    sorts[firstSortIndex].isAsc()
                        ? new SortValueComparator(sorts[firstSortIndex])
                        : new SortValueComparator(sorts[firstSortIndex]).reversed()
                ).thenComparing(
                    EntityRef::getSecondOrderValue,
                    sorts[secondSortIndex].isAsc()
                        ? new SortValueComparator(sorts[secondSortIndex])
                        : new SortValueComparator(sorts[secondSortIndex]).reversed()
                )
            );
        } else if (sorts.length == hasFirstSortLen) {
            // 单字段排序.
            return refStream.sorted(
                Comparator.comparing(
                    EntityRef::getOrderValue,
                    sorts[firstSortIndex].isAsc()
                        ? new SortValueComparator(sorts[firstSortIndex])
                        : new SortValueComparator(sorts[firstSortIndex]).reversed()
                ));
        }

        return refStream;
    }

    // 创建一个新的Page,新的 pageSize 可以指定一个以原有 page.pageSize 的提升百分比.
    private Page createIndexPage(Page page, float overflowRatio) {
        if (page.isEmptyPage()) {
            return Page.emptyPage();
        }

        // 增加相应比例.
        float overflow = Math.round((float) page.getPageSize() * overflowRatio);
        long pageSize = page.getPageSize() + (long) overflow;
        Page newPage;
        if (page.isSinglePage()) {
            newPage = Page.newSinglePage(pageSize);
        } else {
            newPage = new Page(page.getIndex(), pageSize);
        }
        // 如果有软上限,那么也增加相应比例.
        if (page.hasVisibleTotalCountLimit()) {
            overflow = Math.round((float) page.getVisibleTotalCount() * overflowRatio);
            newPage.setVisibleTotalCount(page.getVisibleTotalCount() + (long) overflow);
        }

        return newPage;
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

    // 比较器.
    static class SortValueComparator implements Comparator<String> {

        private Sort sort;

        public SortValueComparator(Sort sort) {
            this.sort = sort;
        }

        @Override
        public int compare(String o1, String o2) {
            return sort.getField().type().compareFromStringValue(o1, o2);
        }
    }

    // 如果排序,但是查询结果没有值.
    private Collection<EntityRef> fixNullSortValue(Collection<EntityRef> refs, Sort[] sorts) {
        int sortIndex = 0;
        Sort sort;
        for (int i = 0; i < sorts.length; i++) {
            sort = sorts[i];
            if (!sort.isOutOfOrder()) {
                for (EntityRef r : refs) {
                    if (!haveSortValue(r, i)) {
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

    private boolean haveSortValue(EntityRef r, int sortIndex) {
        switch (sortIndex) {
            case 0: {
                return !(r.getOrderValue() == null || r.getOrderValue().isEmpty());
            }
            case 1: {
                return !(r.getSecondOrderValue() == null || r.getSecondOrderValue().isEmpty());
            }
            case 2: {
                return !(r.getThridOrderValue() == null || r.getThridOrderValue().isEmpty());
            }
            default: {
                return false;
            }
        }
    }

    private void setSortValue(int sortIndex, EntityRef ref, String value) {
        // 如果是意外的字段,那么设置为一个字符串0,数字和字符串都可以正常转型.
        final String finalValue = "0";
        ref.setSortValue(sortIndex, value);
        if (!ref.getSortValue(sortIndex).isPresent()) {
            ref.setSortValue(sortIndex, finalValue);
        }
    }

    // 构造当前查询的最小提交号.
    private long getQueryCommitId() {
        long minUnSyncCommitId = CommitIdStatusService.INVALID_COMMITID;
        if (commitIdStatusService != null) {
            // 获取提交号.
            minUnSyncCommitId = commitIdStatusService.getMinWithKeep();
            if (minUnSyncCommitId == CommitIdStatusService.INVALID_COMMITID) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Unable to fetch the commit number, use the default commit number 0.");
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "The minimum commit number {} that is currently uncommitted was successfully obtained.",
                        minUnSyncCommitId);
                }
            }
        }
        return minUnSyncCommitId;
    }

    // 构造查询提交号.
    private long buildQueryCommitId(SelectConfig config) {
        // 由外部提供提交号,一般用以测试使用.
        if (config.getCommitId() > 0) {
            return config.getCommitId();
        }

        long minUnSyncCommitId = getQueryCommitId();

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
