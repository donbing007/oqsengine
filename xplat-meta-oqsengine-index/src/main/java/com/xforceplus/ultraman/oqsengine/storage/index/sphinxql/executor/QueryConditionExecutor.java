package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.pojo.page.PageScope;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.constant.SQLConstant;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.SqlKeywordDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.pojo.SortField;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.pojo.SphinxQLWhere;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.commit.CommitHelper;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValueFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import io.vavr.Tuple3;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Query condition Executor.
 */
public class QueryConditionExecutor
    extends AbstractJdbcTaskExecutor<Tuple3<IEntityClass, Conditions, SelectConfig>, List<EntityRef>> {

    Logger logger = LoggerFactory.getLogger(QueryConditionExecutor.class);

    private StorageStrategyFactory storageStrategyFactory;

    private SphinxQLConditionsBuilderFactory conditionsBuilderFactory;

    /**
     * 实例化.
     *
     * @param indexTableName           索引名称.
     * @param resource                 事务资源.
     * @param conditionsBuilderFactory 条件构造器工厂.
     * @param storageStrategyFactory   逻辑物理字段转换器工厂.
     * @param queryTimeMs              最大查询超时毫秒.
     */
    public QueryConditionExecutor(
        String indexTableName,
        TransactionResource<Connection> resource,
        SphinxQLConditionsBuilderFactory conditionsBuilderFactory,
        StorageStrategyFactory storageStrategyFactory,
        long queryTimeMs) {

        super(indexTableName, resource, queryTimeMs);
        this.conditionsBuilderFactory = conditionsBuilderFactory;
        this.storageStrategyFactory = storageStrategyFactory;
    }

    /**
     * 构造方法.
     *
     * @param indexTableName           索引名称.
     * @param resource                 事务资源.
     * @param conditionsBuilderFactory 条件构造器工厂.
     * @param storageStrategyFactory   逻辑物理字段转换器工厂.
     * @param maxQueryTimeMs           最大查询超时毫秒.
     * @return 实例.
     */
    public static Executor<Tuple3<IEntityClass, Conditions, SelectConfig>, List<EntityRef>> build(
        String indexTableName,
        TransactionResource<Connection> resource,
        SphinxQLConditionsBuilderFactory conditionsBuilderFactory,
        StorageStrategyFactory storageStrategyFactory,
        Long maxQueryTimeMs) {

        return new QueryConditionExecutor(indexTableName, resource, conditionsBuilderFactory, storageStrategyFactory,
            maxQueryTimeMs);
    }

    // 构造排序查询语句段.
    private String buildOrderBySqlSegment(List<SortField> sortFields) {
        if (sortFields.isEmpty()) {
            return "";
        }

        StringBuilder buff = new StringBuilder();

        buff.append(SqlKeywordDefine.ORDER).append(' ');

        boolean first = true;
        int index = 0;
        for (SortField sortField : sortFields) {

            if (!first && buff.length() > 0) {
                buff.append(",");
            }

            first = false;

            if (sortField.isIdentifie()) {
                buff.append(FieldDefine.ID)
                    .append(' ')
                    .append(sortField.isDesc() ? SqlKeywordDefine.ORDER_TYPE_DESC : SqlKeywordDefine.ORDER_TYPE_ASC);
            } else {
                buff.append(FieldDefine.SORT_FIELD_ALIAS_PREFIX).append(index++)
                    .append(' ')
                    .append(sortField.isDesc() ? SqlKeywordDefine.ORDER_TYPE_DESC : SqlKeywordDefine.ORDER_TYPE_ASC);
            }

        }
        return buff.toString();
    }

    // 构造排序要用到的字段在select段.
    // select id,cref,pref, attr.123123L as sort1 from
    private String buildSortSelectValuesSegment(List<SortField> sortFields) {
        if (sortFields.isEmpty()) {
            return "";
        }

        StringBuilder buff = new StringBuilder();
        String fieldName;
        int index = 0;
        for (SortField sortField : sortFields) {
            if (buff.length() > 0) {
                buff.append(',');
            }

            // id 不需要额外增加排序字段.
            if (sortField.isIdentifie()) {
                continue;
            }

            if (sortField.isNumber()) {
                if (!sortField.isSystem()) {
                    fieldName = "bigint(" + sortField.getFieldName() + ")";
                } else {
                    fieldName = sortField.getFieldName();
                }
            } else {
                fieldName = sortField.getFieldName();
            }

            buff.append(fieldName)
                .append(' ').append(SqlKeywordDefine.ALIAS_LINK).append(' ')
                .append(FieldDefine.SORT_FIELD_ALIAS_PREFIX).append(index++);
        }

        if (buff.length() > 0) {
            // 为首个增加一个','分隔.
            buff.insert(0, ',');
            // 如果尾部多了一个',',删除.
            if (buff.charAt(buff.length() - 1) == ',') {
                buff.deleteCharAt(buff.length() - 1);
            }
        }
        return buff.toString();
    }

    // 构造排序字段信息.
    private List<SortField> buildSortValues(Sort sort) {
        List<SortField> sortFields = null;
        if (!sort.isOutOfOrder()) {

            if (sortFields == null) {
                sortFields = new ArrayList<>();
            }


            /*
             * optimize: 这里进行一个可能的优化,尝试将排序字段优化成原生字段.
             *           如果可以使用原生字段字段,即非 NORMAL 类型的字段.
             */
            List<SortField> nativeSortFields = tryNativeFieldSorting(sort.getField(), sort.isDes());
            if (!nativeSortFields.isEmpty()) {
                sortFields.addAll(nativeSortFields);
                return sortFields;
            }

            StorageStrategy storageStrategy = storageStrategyFactory.getStrategy(sort.getField().type());
            Collection<String> storageNames = storageStrategy.toStorageNames(sort.getField(), true);

            String fieldName;
            boolean number;
            StringBuilder buff = new StringBuilder();
            for (String storageName : storageNames) {
                buff.delete(0, buff.length());
                buff.append(FieldDefine.ATTRIBUTE).append(".").append(storageName);
                fieldName = buff.toString();

                if (storageStrategy.storageType() == StorageType.LONG) {
                    number = true;
                } else {
                    number = false;
                }

                sortFields.add(
                    SortField.Builder.anSortField()
                        .withFieldName(fieldName)
                        .withNumber(number)
                        .withDesc(sort.isDes())
                        .withStick(storageStrategy.isMultipleStorageValue())
                        .withField(sort.getField()).build());
            }

            return sortFields;
        }

        return Collections.emptyList();
    }

    // 尝试使用原生的字段排序.
    private List<SortField> tryNativeFieldSorting(IEntityField field, boolean desc) {
        // 如果是主键.
        if (field.config().isIdentifie()) {
            return Arrays.asList(
                SortField.Builder.anSortField()
                    .withFieldName("id")
                    .withIdentifie(true)
                    .withDesc(desc)
                    .build()
            );
        }

        FieldConfig.FieldSense sence = field.config().getFieldSense();
        // 中会有一个排序字段.
        final int onlyOne = 1;
        List<SortField> sortFields = new ArrayList<>(onlyOne);
        switch (sence) {
            case CREATE_TIME: {
                sortFields.add(
                    SortField.Builder.anSortField()
                        .withFieldName(FieldDefine.CREATE_TIME)
                        .withField(field)
                        .withNumber(true)
                        .withSystem(true)
                        .withDesc(desc)
                        .build());
                break;
            }
            case UPDATE_TIME: {
                sortFields.add(
                    SortField.Builder.anSortField()
                        .withFieldName(FieldDefine.UPDATE_TIME)
                        .withField(field)
                        .withNumber(true)
                        .withSystem(true)
                        .withDesc(desc)
                        .build());
                break;
            }
            default: {
                // do nothing
            }
        }
        return sortFields;
    }

    @Override
    public List<EntityRef> execute(Tuple3<IEntityClass, Conditions, SelectConfig> queryCondition) throws SQLException {

        Conditions conditions = queryCondition._2();
        IEntityClass entityClass = queryCondition._1();
        Set<Long> filterIds = queryCondition._3().getExcludedIds();
        long commitId = queryCondition._3().getCommitId();
        Conditions filterConditions = queryCondition._3().getDataAccessFilterCondtitions();

        SphinxQLWhere where = conditionsBuilderFactory.getBuilder(conditions).build(conditions, entityClass);
        /*
         * 如果有数据过滤条件,那么将默认以OR=true,range=true的方式找到条件构造器.
         * 目的是防止进入全文字段.
         */
        if (!filterConditions.isEmtpy()) {
            SphinxQLWhere filterWhere =
                conditionsBuilderFactory.getBuilder(true, true).build(filterConditions, entityClass);

            where.addWhere(filterWhere, true);

        }


        if (filterIds != null && !filterIds.isEmpty()) {
            for (long id : filterIds) {
                where.addFilterId(id);
            }
        }

        if (commitId > 0 && commitId < CommitHelper.getUncommitId()) {
            where.setCommitId(commitId);
        }

        where.addEntityClass(entityClass);

        Page page = queryCondition._3().getPage();
        if (!page.isSinglePage()) {
            page.setTotalCount(Long.MAX_VALUE);
        }
        PageScope scope = page.getNextPage();

        /*
         * 当没有下一页,并且不是空页要求时直接空返回.
         * 当是空页要求时,会继续计算但不会返回任何数据只是会填充数据总量.
         */
        if (scope == null && !page.isEmptyPage()) {
            return Collections.emptyList();
        }

        Sort[] sorts = new Sort[] {
            queryCondition._3().getSort(),
            queryCondition._3.getSecondarySort(),
            queryCondition._3.getThirdSort()
        };
        String orderBySqlSegment = "";
        String sortSelectValuesSegment = "";
        List<SortField> sortFields = new ArrayList<>(3);

        /*
         * 空页要求时不需要进行排序.
         */
        if (!page.isEmptyPage()) {
            for (Sort sort : sorts) {
                sortFields.addAll(buildSortValues(sort));
            }
            orderBySqlSegment = buildOrderBySqlSegment(sortFields);
            sortSelectValuesSegment = buildSortSelectValuesSegment(sortFields);
        }

        String sql = String.format(
            SQLConstant.SELECT_SQL, sortSelectValuesSegment, getTableName(), where.toString(), orderBySqlSegment);

        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
            st.setLong(1, 0);
            if (page.isEmptyPage()) {
                st.setLong(2, 0);
            } else {
                st.setLong(2, page.getPageSize() * (page.getIndex() - 1));
            }
            long maxMatch = page.getPageSize() * (page.getIndex() - 1);
            if (page.hasVisibleTotalCountLimit()) {
                maxMatch = maxMatch > page.getVisibleTotalCount() ? page.getVisibleTotalCount() : maxMatch;
            }
            st.setLong(3, maxMatch <= 0 ? 1 : maxMatch);
            // 设置manticore的查询超时时间.
            st.setLong(4, getTimeoutMs());

            try (ResultSet rs = st.executeQuery()) {
                List<EntityRef> refs = new ArrayList((int) page.getPageSize());

                while (rs.next()) {
                    EntityRef.Builder entityRefBuilder = EntityRef.Builder.anEntityRef();
                    long id = rs.getLong(FieldDefine.ID);
                    entityRefBuilder.withId(id);

                    processSort(id, rs, entityRefBuilder, sortFields);
                    refs.add(entityRefBuilder.build());

                }

                if (!page.isSinglePage()) {
                    long count = SphinxQLHelper.count(getResource());
                    page.setTotalCount(count);
                } else {
                    page.setTotalCount(refs.size());
                }

                return refs;
            }
        }
    }

    private void processSort(long id, ResultSet rs, EntityRef.Builder entityRefBuilder, List<SortField> sortFields)
        throws SQLException {
        StorageValue lastStorageValue = null;
        int sortIndex = 0;
        SortField sortField = null;
        for (int s = 0; s < sortFields.size(); s++) {
            sortField = sortFields.get(s);
            if (sortField.isIdentifie()) {
                setOrderValue(sortIndex++, Long.toString(id), entityRefBuilder);
                continue;
            }

            StorageStrategy storageStrategy =
                storageStrategyFactory.getStrategy(sortField.getField().type());
            String sortFieldName =
                String.format("%s%d", FieldDefine.SORT_FIELD_ALIAS_PREFIX, s);

            StorageValue currentStorageValue;
            switch (storageStrategy.storageType()) {
                case LONG: {
                    currentStorageValue = StorageValueFactory.buildStorageValue(
                        storageStrategy.storageType(),
                        sortFieldName,
                        rs.getLong(sortFieldName)
                    );
                    break;
                }
                case STRING: {
                    currentStorageValue = StorageValueFactory.buildStorageValue(
                        storageStrategy.storageType(),
                        sortFieldName,
                        rs.getString(sortFieldName)
                    );
                    break;
                }
                default: {
                    throw new SQLException(String.format("Unrecognized physical field type.[%s]",
                        storageStrategy.storageType().name()));
                }
            }

            if (sortField.isStick()) {
                if (lastStorageValue == null) {
                    lastStorageValue = currentStorageValue;
                    lastStorageValue.locate(0);
                } else {
                    StorageValue point = lastStorageValue;
                    while (point.haveNext()) {
                        point = lastStorageValue.next();
                    }
                    int loc = point.location();
                    currentStorageValue.locate(loc + 1);
                    lastStorageValue.stick(currentStorageValue);
                }
            } else {
                IValue logicValue;
                /*
                 * 发现了一个非需要粘合的搜索结果字段,所以把之前积累的 lastStorageValue 转换成IValue.
                 */
                if (lastStorageValue != null) {
                    logicValue =
                        storageStrategy.toLogicValue(sortFields.get(s - 1).getField(), lastStorageValue);
                    setOrderValue(sortIndex++, logicValue.valueToString(), entityRefBuilder);
                }

                logicValue = storageStrategy.toLogicValue(sortFields.get(s).getField(), currentStorageValue);
                setOrderValue(sortIndex++, logicValue.valueToString(), entityRefBuilder);

                lastStorageValue = null;
            }
        }

        if (lastStorageValue != null) {
            StorageStrategy storageStrategy =
                storageStrategyFactory.getStrategy(sortField.getField().type());
            IValue logicValue = storageStrategy.toLogicValue(sortField.getField(), lastStorageValue);
            setOrderValue(sortIndex++, logicValue.valueToString(), entityRefBuilder);
        }
    }

    private void setOrderValue(int i, String value, EntityRef.Builder entityRefBuilder) {
        switch (i) {
            case 0: {
                entityRefBuilder.withOrderValue(value);
                break;
            }
            case 1: {
                entityRefBuilder.withSecondOrderValue(value);
                break;
            }
            case 2: {
                entityRefBuilder.withThridOrderValue(value);
                break;
            }
            default: {
                // nothing
            }
        }
    }
}