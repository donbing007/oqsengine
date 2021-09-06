package com.xforceplus.ultraman.oqsengine.changelog.impl;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.xforceplus.ultraman.oqsengine.changelog.ReplayService;
import com.xforceplus.ultraman.oqsengine.changelog.domain.*;
import com.xforceplus.ultraman.oqsengine.changelog.entity.ChangelogStatefulEntity;
import com.xforceplus.ultraman.oqsengine.changelog.storage.write.ChangelogStorage;
import com.xforceplus.ultraman.oqsengine.changelog.storage.write.SnapshotStorage;
import com.xforceplus.ultraman.oqsengine.changelog.utils.ChangelogHelper;
import com.xforceplus.ultraman.oqsengine.changelog.utils.EntityClassHelper;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeValue.Op.SET;
import static com.xforceplus.ultraman.oqsengine.changelog.utils.ChangelogHelper.*;

/**
 * rebuild from changlog
 */
public class ReplayServiceImpl implements ReplayService {

    private LoadingCache<Tuple2<Long, Long>, ChangelogStatefulEntity> cache =
           Caffeine.newBuilder().maximumSize(1000L).build((x) ->  {
                    return replayStatefulEntityInternal(x._1(), x._2()).orElse(null);
            });


    private Logger logger = LoggerFactory.getLogger(ReplayService.class);

    @Resource
    private ChangelogStorage changelogStorage;

    @Resource
    private SnapshotStorage snapshotStorage;

    @Resource
    private MetaManager metaManager;


    private long snapshotThreshold = 100;


    /**
     * find all related changelog
     *
     * @param id
     * @return
     */
    @Override
    public List<Changelog> getRelatedChangelog(long id) {
        return changelogStorage.findById(id, -1, -1);
    }

    /**
     * TODO consider the snapshot
     *
     * @param id
     * @param endVersion
     * @param startVersion
     * @return
     */
    @Override
    public List<Changelog> getRelatedChangelog(long id, long startVersion, long endVersion) {
        return changelogStorage.findById(id, startVersion, endVersion);
    }

    @Override
    public EntityDomain replaySimpleDomain(long entityClass, long id, long endVersion) {

        Tuple2<ChangeSnapshot, List<Changelog>> changeTuple = getChangeTuple(id, endVersion);
        Optional<IEntityClass> entityClassOp = metaManager.load(entityClass);
        return entityClassOp
                .map(iEntityClass -> replaySingleDomainWithSnapshot(iEntityClass
                        , id
                        , changeTuple._1()
                        , changeTuple._2()))
                .orElse(null);
    }

    private Tuple2<ChangeSnapshot, List<Changelog>> getChangeTuple(long id, long endVersion){
        Optional<ChangeSnapshot> snapshotOp = snapshotStorage.query(id, endVersion);

        long startVersion = -1;

        if (snapshotOp.isPresent()) {
            ChangeSnapshot changeSnapshot = snapshotOp.get();
            startVersion = changeSnapshot.getVersion();
        }

        List<Changelog> relatedChangelogs = this.getRelatedChangelog(id, startVersion, endVersion);
        return Tuple.of(snapshotOp.orElse(null), relatedChangelogs);
    }

    /**
     * side effect
     *
     * @param historyValues
     */
    private void linkAllHistory(List<HistoryValue> historyValues) {
        historyValues.stream().reduce((a, b) -> {
            b.setNext(a);
            a.setPreview(b);
            return b;
        });
    }

    /**
     * strong weak?
     *
     * @param entityClass
     * @param id
     * @param changelogList
     * @return
     */
    @Override
    public EntityRelation replayRelation(IEntityClass entityClass, long id, List<Changelog> changelogList) {

        EntityRelation entityRelation = new EntityRelation();
        entityRelation.setId(id);

        Map<Long, List<HistoryValue>> historyValueMapping = ChangelogHelper.getMappedHistoryValue(changelogList);
        Map<Relationship, Collection<ValueLife>> relationValues = new HashMap<>();

        entityRelation.setRelatedIds(relationValues);

        Map<Long, Map<String, ValueLife>> valueMap = new HashMap<>();


        /**
         * deal every relations to find out history and current value and record in relationValues
         */
        Optional.ofNullable(entityClass.relationship()).orElse(Collections.emptyList()).stream()
                .filter(x -> x.isStrong() && !x.isCompanion())
                .forEach(x -> {
                    //get value from changelogList;
                    List<HistoryValue> historyValues = historyValueMapping.get(x.getEntityField().id());

                    if (historyValues != null) {
                        //link all
                        linkAllHistory(historyValues);
                        //travel every history value
                        historyValues.stream().sorted().forEach(historyValue -> {
                            ChangeValue value = historyValue.getValue();
                            Map<String, ValueLife> valueLifeMap = valueMap
                                    .computeIfAbsent(x.getEntityField().id(), k -> new HashMap<>());

                            ValueLife valueLife = valueLifeMap.get(value.getRawValue());
                            if (valueLife == null) {
                                valueLife = new ValueLife();
                                valueLife.setValue(value.getRawValue());
                                valueLifeMap.put(value.getRawValue(), valueLife);
                            }

                            /**
                             * SET |  (ADD | DEL) will not mix with each other
                             */
                            switch (value.getOp()) {
                                case ADD:
                                    valueLife.setStart(historyValue.getCommitId());
                                    valueLife.setEnd(-1);
                                    break;
                                case DEL:
                                    valueLife.setEnd(historyValue.getCommitId());
                                    break;
                                case SET:
                                    valueLife.setStart(historyValue.getCommitId());
                                    valueLife.setEnd(-1);
                                    if (historyValue.getPreview() != null) {
                                        ChangeValue previewChange = historyValue.getPreview().getValue();
                                        String rawValue = previewChange.getRawValue();
                                        ValueLife previewValueLife = valueLifeMap.get(rawValue);
                                        if (previewValueLife != null) {
                                            previewValueLife.setEnd(historyValue.getCommitId());
                                        }
                                    }
                                    break;
                                default:
                            }
                        });

                        relationValues.put(x, valueMap.get(x.getEntityField().id()).values());
                    }
                });

        return entityRelation;
    }

    /**
     * BFS
     *
     * @param entityClass
     * @param id
     * @param version
     * @return
     */
    @Override
    public EntityAggDomain replayAggDomain(long entityClass, long id, long version) {

        Queue<Tuple3<Long, Long, Long>> taskQueue = new LinkedList<>();

        Map<Long, EntityDomain> footprint = new HashMap<>();

        taskQueue.offer(Tuple.of(entityClass, id, version));
        while (!taskQueue.isEmpty()) {
            Tuple3<Long, Long, Long> task = taskQueue.poll();

            Optional<IEntityClass> entityClassOptional = metaManager.load(task._1);
            if (entityClassOptional.isPresent()) {


                Tuple2<ChangeSnapshot, List<Changelog>> changelogTuple = getChangeTuple(id, version);
                EntityDomain entityDomain = replaySingleDomainWithSnapshot(entityClassOptional.get()
                        , task._2, changelogTuple._1(), changelogTuple._2());
                footprint.put(task._2, entityDomain);
                entityDomain.getReferenceMap().forEach((key, value) -> {
                    //put in
                    value.forEach(eachId -> {
                        taskQueue.offer(Tuple.of(key.getRightEntityClassId(), eachId, version));
                    });
                });
            }
        }
        EntityAggDomain head = new EntityAggDomain();
        buildAggDomain(id, footprint, head);
        return head;
    }

    private Optional<ChangelogStatefulEntity> replayStatefulEntityInternal(long entityClassId, long id) {

        Optional<IEntityClass> loadEntityClassOp = metaManager.load(entityClassId);
        return loadEntityClassOp.map(x -> {
            EntityDomain entityDomain = replaySimpleDomain(entityClassId, id, -1);
            ChangelogStatefulEntity statefulEntity = new ChangelogStatefulEntity(id, x, metaManager, entityDomain, snapshotThreshold);
            return statefulEntity;
        });
    }


    /**
     * @param entityClassId
     * @param id
     * @return
     * @Throws runtime exception
     */
    @Override
    public Optional<ChangelogStatefulEntity> replayStatefulEntity(long entityClassId, long id) {
        return Optional.ofNullable(cache.get(Tuple.of(entityClassId, id)));
    }

    private void buildAggDomain(long id, Map<Long, EntityDomain> map, EntityAggDomain head) {

        /**
         * current id, currentDomain, preview AggDomain
         */
        Queue<Tuple3<Long, EntityAggDomain, Tuple2<Relationship, EntityAggDomain>>> taskIdQueue = new LinkedList<>();

        taskIdQueue.offer(Tuple.of(id, head, null));
        //BFS
        while (!taskIdQueue.isEmpty()) {
            Tuple3<Long, EntityAggDomain, Tuple2<Relationship, EntityAggDomain>> task = taskIdQueue.poll();

            Long currentNodeId = task._1;
            EntityAggDomain currentEntityAgg = task._2;
            Tuple2<Relationship, EntityAggDomain> previewEntityRelToAgg = task._3;

            EntityDomain entityDomain = map.get(currentNodeId);
            if (entityDomain != null) {
                currentEntityAgg.setRootIEntity(entityDomain.getEntity());
                if (previewEntityRelToAgg != null) {
                    previewEntityRelToAgg._2().put(previewEntityRelToAgg._1(), currentEntityAgg);
                }
                entityDomain.getReferenceMap().forEach((key, value) -> {
                    value.forEach(refId -> {
                        EntityAggDomain entityAggDomain = new EntityAggDomain();
                        taskIdQueue.offer(Tuple.of(refId, entityAggDomain, Tuple.of(key, currentEntityAgg)));
                    });
                });
            }
        }
    }

    private IValue getValue(List<ChangeValue> changeValues, IEntityField field) {
        Optional<ChangeValue> firstSet = changeValues.stream().filter(x -> x.getOp() == SET).findFirst();
        if (firstSet.isPresent()) {
            ChangeValue changeValue = firstSet.get();
            String rawValue = changeValue.getRawValue();
            IValue value = deserialize(rawValue, field);
            return value;
        }

        return null;
    }

    /**
     * replay with snapshot
     *
     * @param entityClass
     * @param id
     * @param changeSnapshot
     * @param changelogs
     * @return
     */
    private EntityDomain replaySingleDomainWithSnapshot(IEntityClass entityClass, long id, ChangeSnapshot changeSnapshot, List<Changelog> changelogs) {

        IEntityValue entityValue = EntityValue.build();
        IEntity entity = Entity.Builder.anEntity()
                .withId(id)
                .withEntityClassRef(EntityClassRef.Builder
                        .anEntityClassRef()
                        .withEntityClassId(entityClass.id())
                        .build())
                .withEntityValue(entityValue).build();

        Map<Relationship, List<Long>> referenceMap = new HashMap<>();

        EntityDomain entityDomain;
        if(!changelogs.isEmpty()) {
            entityDomain = new EntityDomain(changelogs.size(), changelogs.get(0).getVersion(), entity, referenceMap);
        } else {
            entityDomain = new EntityDomain(changelogs.size(), 0, entity, referenceMap);
        }

        Map<Long, List<ChangeValue>> mappedValue = new HashMap<>();


        if (changeSnapshot != null) {
            /**
             * init reference map with snapshot
             */
            changeSnapshot.getReferenceMap().forEach((k, v) -> {
                Optional<Relationship> relationWithFieldId = EntityClassHelper.findRelationWithFieldId(entityClass, k);
                relationWithFieldId.ifPresent(x -> {
                    referenceMap.put(x, v);
                });
            });

            /**
             * init mappedValue with snapshot
             */
            mappedValue = changeSnapshot.getChangeValues().stream().collect(Collectors.toMap(ChangeValue::getFieldId, x -> {
                List<ChangeValue> changeValues = new LinkedList<>();
                changeValues.add(x);
                return changeValues;
            }));
        }

        //init with snapshot
        //put current changelogs
        Map<Long, List<ChangeValue>> finalMappedValue = mappedValue;
        Map<Long, List<ChangeValue>> mappedValueFromChangelogs = getMappedValue(changelogs);

        mappedValueFromChangelogs.forEach((a, b) -> {
            if (finalMappedValue.containsKey(a)) {
                List<ChangeValue> changeValues = finalMappedValue.get(a);
                if (changeValues != null) {
                    changeValues.addAll(b);
                } else {
                    finalMappedValue.put(a, b);
                }
            } else {
                finalMappedValue.put(a, b);
            }
        });


        /**
         * check every field current value
         */
        entityClass.fields().forEach(field -> {
            //TODO check order
            List<ChangeValue> changeValues = finalMappedValue.get(field.id());
            IValue value = null;
            if (changeValues != null) {
                value = getValue(changeValues, field);
            }

            if (value != null) {
                entityValue.addValue(value);
            }
        });

        /**
         * store relation field in reference map
         */
        Optional.ofNullable(entityClass.relationship()).orElse(Collections.emptyList()).forEach(rel -> {
//            if (rel.getFieldOwner() != entityClass.id()) {
                //current entityClass do not have this field
                List<ChangeValue> changeValues = Optional.ofNullable(finalMappedValue.get(rel.getEntityField().id())).orElseGet(Collections::emptyList);
                if (isReferenceSetInCurrentView(rel, entityClass.id())) {
                    List<Long> ids = new LinkedList<>();
                    changeValues.forEach(changeValue -> {
                        switch (changeValue.getOp()) {
                            case ADD:
                                ids.add(Long.parseLong(changeValue.getRawValue()));
                                break;
                            case DEL:
                                ids.remove(Long.parseLong(changeValue.getRawValue()));
                                break;
                            default:
                                logger.warn("unsupport operation for referenceset");
                        }
                    });

                    if (!ids.isEmpty()) {
                        referenceMap.put(rel, ids);
                    }
                } else {
                    IValue value = getValue(changeValues, rel.getEntityField());
                    if (value != null) {
                        //caution should be
                        List<Long> ids = new ArrayList<>();
                        ids.add(value.valueToLong());
                        referenceMap.put(rel, ids);
                    }
                }
//           }
        });
        return entityDomain;
    }

//    /**
//     * replay the changelogs to ane entityDomain
//     * @param entityClass
//     * @param id
//     * @param changelogs
//     * @return
//     */
//    private EntityDomain replaySingleDomain(IEntityClass entityClass, long id, List<Changelog> changelogs){
//
//        EntityValue entityValue = new EntityValue(id);
//        IEntity entity = Entity.Builder.anEntity()
//                .withId(id)
//                .withEntityClass(entityClass)
//                .withEntityValue(entityValue).build();
//
//        Map<OqsRelation, List<Long>> referenceMap = new HashMap<>();
//
//        EntityDomain entityDomain = new EntityDomain(changelogs.size(), entity, referenceMap);
//
//        Map<Long, List<ChangeValue>> mappedValue = ChangelogHelper.getMappedValue(changelogs);
//
//        /**
//         * check every field current value
//         */
//        entityClass.fields().forEach(field -> {
//            //TODO check order
//            List<ChangeValue> changeValues = mappedValue.get(field.id());
//            IValue value = null;
//            if(changeValues != null) {
//                 value = getValue(changeValues, field);
//            }
//            if(value != null){
//                entityValue.addValue(value);
//            }
//        });
//
//        /**
//         * deal relation value
//         */
//        Optional.ofNullable(entityClass.oqsRelations()).orElse(Collections.emptyList()).forEach(rel -> {
//            if(rel.getFieldOwner() != entityClass.id()){
//                //current entityClass do not have this field
//                List<ChangeValue> changeValues = mappedValue.get(rel.getEntityField().id());
//                if(isReferenceSetInCurrentView(rel, entityClass.id())){
//                    List<Long> ids = new LinkedList<>();
//                    changeValues.forEach(changeValue -> {
//                        switch(changeValue.getOp()) {
//                            case ADD:
//                                ids.add(Long.parseLong(changeValue.getRawValue()));
//                                break;
//                            case DEL:
//                                ids.remove(Long.parseLong(changeValue.getRawValue()));
//                                break;
//                            default:
//                                logger.warn("unsupport operation for referenceset");
//                        }
//                    });
//
//                    if(!ids.isEmpty()) {
//                        referenceMap.put(rel, ids);
//                    }
//                } else {
//                    IValue value = getValue(changeValues, rel.getEntityField());
//                    if(value != null){
//                        referenceMap.put(rel, Collections.singletonList(value.valueToLong()));
//                    }
//                }
//            }
//        });
//
//        return entityDomain;
//    }
}
