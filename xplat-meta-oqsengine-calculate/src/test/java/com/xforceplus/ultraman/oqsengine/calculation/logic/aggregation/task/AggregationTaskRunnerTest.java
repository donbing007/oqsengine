package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.task;

import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl.MetaParseTree;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl.PTNode;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import com.xforceplus.ultraman.oqsengine.storage.pojo.search.SearchConfig;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 聚合任务执行测试.
 *
 * @author weikai
 * @version 1.0 2021/9/6 13:37
 * @since 1.8
 */
public class AggregationTaskRunnerTest {
    private long aggEntityClassId = 100;
    private long targetEntityClassId = 200;

    private long aggFieldId = 1000;
    private long targetFieldId = 1001;

    private IEntityField aggField = EntityField.Builder.anEntityField()
        .withId(aggFieldId)
        .withFieldType(FieldType.LONG)
        .withName("agg")
        .withConfig(
            FieldConfig.Builder.anFieldConfig().build()
        ).build();

    private IEntityField targetField = EntityField.Builder.anEntityField()
        .withId(targetFieldId)
        .withFieldType(FieldType.LONG)
        .withName("target")
        .withConfig(
            FieldConfig.Builder.anFieldConfig().build()
        ).build();

    private IEntityClass aggEntityClass = EntityClass.Builder.anEntityClass()
        .withId(aggEntityClassId)
        .withCode("agg")
        .withField(aggField)
        .withLevel(0)
        .withRelations(
            Arrays.asList(
                Relationship.Builder.anRelationship()
                    .withId(Long.MAX_VALUE)
                    .withCode(Long.toString(Long.MAX_VALUE))
                    .withEntityField(targetField)
                    .withLeftEntityClassId(targetEntityClassId)
                    .withRightEntityClassId(aggEntityClassId)
                    .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                    .withBelongToOwner(false)
                    .withStrong(true)
                    .withIdentity(true)
                    .build()
            )
        ).build();


    private IEntityClass targetEntityClass = EntityClass.Builder.anEntityClass()
        .withId(targetEntityClassId)
        .withCode("target")
        .withLevel(0)
        .withField(targetField)
        .withRelations(
            Arrays.asList(
                Relationship.Builder.anRelationship()
                    .withId(Long.MAX_VALUE - 1)
                    .withCode(Long.toString(Long.MAX_VALUE))
                    .withLeftEntityClassId(targetEntityClassId)
                    .withRightEntityClassId(aggEntityClassId)
                    .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                    .withStrong(true)
                    .withBelongToOwner(false)
                    .withIdentity(true)
                    .build()
            )
        )
        .build();


    private AggregationTaskRunner runner;
    private PTNode ptNode;
    private List<PTNode> nodes;
    private CommitIdStatusService commitIdStatusService;
    private IndexStorage indexStorage;
    private MasterStorage masterStorage;
    private long avgEntityId;
    private Map<Long, List<EntityRef>> indexData;

    /**
     * 初始化.
     */
    @BeforeEach
    public void before() throws Exception {
        ptNode = new PTNode();
        ptNode.setAggEntityClass(aggEntityClass);
        ptNode.setAggEntityField(aggField);
        ptNode.setEntityClass(targetEntityClass);
        ptNode.setEntityField(targetField);
        ptNode.setAggregationType(AggregationType.MAX);
        ptNode.setRelationship(Relationship.Builder.anRelationship()
            .withId(1)
            .withLeftEntityClassId(targetEntityClassId)
            .withRightEntityClassId(aggEntityClassId)
            .withRightEntityClassLoader(id -> Optional.of(aggEntityClass))
            .withEntityField(aggField)
            .withIdentity(true)
            .withBelongToOwner(true)
            .withStrong(false)
            .withRelationType(Relationship.RelationType.ONE_TO_MANY)
            .build());
        ptNode.setConditions(Conditions.buildEmtpyConditions());
        nodes = new ArrayList<>();
        nodes.add(ptNode);
        indexData = new ConcurrentHashMap<>();

        runner = new AggregationTaskRunner();
        indexStorage = new MockIndexStorage();
        masterStorage = new MockMasterStorage();
        commitIdStatusService = new MockCommitIdStatusService();

        Field masterStorageField = runner.getClass().getDeclaredField("masterStorage");
        masterStorageField.setAccessible(true);
        masterStorageField.set(runner, masterStorage);


        Field indexStorageField = runner.getClass().getDeclaredField("indexStorage");
        indexStorageField.setAccessible(true);
        indexStorageField.set(runner, indexStorage);

        Field commitIdStatusServiceField = runner.getClass().getDeclaredField("commitIdStatusService");
        commitIdStatusServiceField.setAccessible(true);
        commitIdStatusServiceField.set(runner, commitIdStatusService);


    }

    /**
     * 清理.
     */
    @AfterEach
    public void destroy() {
        runner = null;
        ptNode = null;
        masterStorage = null;
        indexStorage = null;
        nodes = null;
        commitIdStatusService = null;
    }

    @Test
    public void run() throws Exception {
        long size = 1000;
        buildData(size);
        runner.run(new AggregationTaskCoordinator(), new AggregationTask("testAgg", new MetaParseTree(ptNode)));
        Optional<IEntity> entity = masterStorage.selectOne(avgEntityId);
        if (entity.isPresent()) {
            Long value = (Long) entity.get().entityValue().getValue(targetFieldId).get().getValue();
            Assertions.assertTrue((size - 1) == value.longValue());
        }
    }

    private void buildData(long size) throws Exception {
        IValue entityValue = new LongValue(targetField, 0);
        IEntity targetEntity = Entity.Builder.anEntity()
            .withId(10000)
            .withEntityClassRef(targetEntityClass.ref())
            .withTime(System.currentTimeMillis())
            .withVersion(0)
            .withEntityValue(EntityValue.build().addValue(entityValue))
            .build();

        avgEntityId = targetEntity.id();
        masterStorage.build(targetEntity, targetEntityClass);

        long baseId = 20000;
        for (int i = 0; i < size; i++) {
            IEntity aggEntity = Entity.Builder.anEntity()
                .withId(baseId++)
                .withEntityClassRef(aggEntityClass.ref())
                .withTime(System.currentTimeMillis())
                .withVersion(0)
                .withEntityValue(
                    EntityValue.build().addValue(new LongValue(aggField, i))
                ).build();

            masterStorage.build(aggEntity, aggEntityClass);
        }
    }


    class MockMasterStorage implements MasterStorage {

        private Map<Long, IEntity> data;

        private Map<Long, List<OriginalEntity>> masterData;

        public MockMasterStorage() {
            data = new ConcurrentHashMap<>();
            masterData = new ConcurrentHashMap<>();
        }


        public Map<Long, IEntity> getData() {
            return data;
        }

        @Override
        public int build(IEntity entity, IEntityClass entityClass) throws SQLException {
            List<OriginalEntity> masterList = new ArrayList<>();
            List<EntityRef> indexList = new ArrayList<>();


            if (masterData.containsKey(entityClass.id())) {
                masterList = masterData.get(entityClass.id());
            }
            if (indexData.containsKey(entityClass.id())) {
                indexList = indexData.get(entityClass.id());
            }


            masterList.add(OriginalEntity.Builder.anOriginalEntity().withId(entity.id()).build());
            masterData.put(entityClass.id(), masterList);

            indexList.add(EntityRef.Builder.anEntityRef().withId(entity.id()).build());
            indexData.put(entityClass.id(), indexList);
            data.put(entity.id(), entity);
            return 1;
        }

        @Override
        public int replace(IEntity entity, IEntityClass entityClass) throws SQLException {
            entity.resetVersion(entity.version() + 1);
            data.put(entity.id(), entity);
            return 1;
        }

        @Override
        public int delete(IEntity entity, IEntityClass entityClass) throws SQLException {
            if (data.remove(entity.id()) != null) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public DataIterator<OriginalEntity> iterator(IEntityClass entityClass, long startTime, long endTime,
                                                     long lastId) throws SQLException {
            MockAbstractDataIterator iterator = new MockAbstractDataIterator();
            try {
                iterator.load(masterData.get(entityClass.id()));
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return iterator;
        }

        @Override
        public DataIterator<OriginalEntity> iterator(IEntityClass entityClass, long startTime, long endTime,
                                                     long lastId, int size) throws SQLException {
            MockAbstractDataIterator iterator = new MockAbstractDataIterator();
            try {
                iterator.load(masterData.get(entityClass.id()));
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return iterator;
        }

        @Override
        public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, SelectConfig config)
            throws SQLException {
            List<OriginalEntity> originalEntities = masterData.get(entityClass.id());
            return originalEntities.stream().map(l -> EntityRef.Builder.anEntityRef().withId(l.getId()).build())
                .collect(Collectors.toList());
        }

        @Override
        public Optional<IEntity> selectOne(long id) throws SQLException {
            return Optional.ofNullable(data.get(id));
        }

        @Override
        public Optional<IEntity> selectOne(long id, IEntityClass entityClass) throws SQLException {
            return Optional.ofNullable(data.get(id));
        }

        @Override
        public Collection<IEntity> selectMultiple(long[] ids) throws SQLException {
            return Arrays.stream(ids).mapToObj(id -> data.get(id)).filter(Objects::nonNull)
                .collect(Collectors.toList());
        }

        @Override
        public Collection<IEntity> selectMultiple(long[] ids, IEntityClass entityClass) throws SQLException {
            return Arrays.stream(ids).mapToObj(id -> data.get(id)).filter(Objects::nonNull)
                .collect(Collectors.toList());
        }

        @Override
        public boolean exist(long id) throws SQLException {
            return data.containsKey(id);
        }
    }


    class MockIndexStorage implements IndexStorage {

        @Override
        public long clean(IEntityClass entityClass, long maintainId, long start, long end) throws SQLException {
            return 0;
        }

        @Override
        public void saveOrDeleteOriginalEntities(Collection<OriginalEntity> originalEntities) throws SQLException {

        }

        @Override
        public Collection<EntityRef> search(SearchConfig config, IEntityClass... entityClasses) throws SQLException {
            return null;
        }

        @Override
        public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, SelectConfig config)
            throws SQLException {
            return indexData.get(entityClass.id());
        }
    }

    /**
     * 数据迭代器mock.
     */
    public static class MockAbstractDataIterator implements DataIterator<OriginalEntity> {

        private final List<OriginalEntity> buffer;

        /**
         * 初始化.
         */

        public MockAbstractDataIterator() {
            this.buffer = new ArrayList<>();
        }


        @Override
        public boolean hasNext() {
            try {
                if (buffer.isEmpty()) {
                    return false;
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }

            return !buffer.isEmpty();
        }

        @Override
        public OriginalEntity next() {
            if (hasNext()) {
                OriginalEntity originalEntity = buffer.remove(0);
                return originalEntity;
            } else {
                return null;
            }
        }

        public void load(List<OriginalEntity> buff) throws NoSuchFieldException, IllegalAccessException {
            buffer.addAll(buff);
        }
    }


    class MockCommitIdStatusService implements CommitIdStatusService {

        @Override
        public boolean save(long commitId, boolean ready) {
            return false;
        }

        @Override
        public boolean isReady(long commitId) {
            return false;
        }

        @Override
        public void ready(long commitId) {

        }

        @Override
        public long[] getUnreadiness() {
            return new long[0];
        }

        @Override
        public Optional<Long> getMin() {
            return Optional.ofNullable(0L);
        }

        @Override
        public Optional<Long> getMax() {
            return Optional.empty();
        }

        @Override
        public long[] getAll() {
            return new long[0];
        }

        @Override
        public long size() {
            return 0;
        }

        @Override
        public void obsolete(long... commitIds) {

        }

        @Override
        public void obsoleteAll() {

        }

        @Override
        public boolean isObsolete(long commitId) {
            return false;
        }
    }


}