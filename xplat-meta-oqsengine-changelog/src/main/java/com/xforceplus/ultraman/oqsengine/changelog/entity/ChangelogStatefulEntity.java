package com.xforceplus.ultraman.oqsengine.changelog.entity;

import com.google.common.collect.Sets;
import com.xforceplus.ultraman.oqsengine.changelog.command.AddChangelog;
import com.xforceplus.ultraman.oqsengine.changelog.command.ChangelogCommand;
import com.xforceplus.ultraman.oqsengine.changelog.domain.*;
import com.xforceplus.ultraman.oqsengine.changelog.event.*;
import com.xforceplus.ultraman.oqsengine.changelog.utils.ChangelogHelper;
import com.xforceplus.ultraman.oqsengine.changelog.utils.EntityClassHelper;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsRelation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TODO
 * a stateful entity
 */
public class ChangelogStatefulEntity implements StatefulEntity<EntityDomain, ChangelogCommand, ChangelogEvent> {

    private Logger logger = LoggerFactory.getLogger(ChangelogStatefulEntity.class);

    /**
     * changelog retrieved
     */
    private long count;

    private long currentVersion;

    private long id;

    private EntityDomain entityDomain;

    private IEntityClass entityClass;

    private MetaManager metaManager;

    private long snapshotThreshold;

    /**
     * TODO init
     */
    public ChangelogStatefulEntity(long id, IEntityClass entityClass, MetaManager metaManager, EntityDomain entityDomain, long snapshotThreshold) {
        this.id = id;
        this.metaManager = metaManager;
        this.entityClass = entityClass;
        this.entityDomain = entityDomain;
        this.count = entityDomain.getCount();
        this.snapshotThreshold = snapshotThreshold;
        this.currentVersion = entityDomain.getVersion();
    }

    @Override
    public List<ChangelogEvent> receive(ChangelogCommand input, Map<String, Object> context) {

        List<ChangelogEvent> retList = new LinkedList<>();
        if (input instanceof AddChangelog) {
            long newVersion = ((AddChangelog) input).getChangedEvent().getCommitId();
            if (newVersion <= currentVersion) {
                //reject the same version
                logger.error("Got old version {} on {}:{}", newVersion, entityClass.id(), id);
                return Collections.emptyList();
            } else {
                ChangedEvent changedEvent = ((AddChangelog) input).getChangedEvent();
                //deal add changelog
                Optional<PersistentEvent> persistentEvent = updateInternalState(changedEvent);
                //add ret event
                persistentEvent.ifPresent(retList::add);
                retList.addAll(genPropagationEvent(changedEvent, context));
                retList.add(genVersionEvent(changedEvent));
                genSnapshotVersionEvent().ifPresent(retList::add);
            }
        } else {
            logger.error("Unknown Command {}", input);
        }
        return retList;
    }

    @Override
    public EntityDomain getState() {
        return entityDomain;
    }

    /**
     * very important
     *
     * @return
     */
    private Optional<SnapshotEvent> genSnapshotVersionEvent() {
        if (count >= snapshotThreshold) {
            logger.warn("Trigger snapshot on {}", id);
            ChangeSnapshot changeSnapshot = new ChangeSnapshot();
            /**
             *     private long version;
             *     private long sId;
             *     private long id;
             *     private long createTime;
             *     private List<ChangeValue> changeValues;
             *     private Map<Long, List<Long>> referenceMap;
             *     private long entityClass;
             */
            changeSnapshot.setVersion(currentVersion);
            changeSnapshot.setId(id);
            Map<Long, List<Long>> referenceMap = new HashMap<>();
            entityDomain.getReferenceMap().forEach((a, b) -> {
                referenceMap.put(a.getEntityField().id(), b);
            });
            changeSnapshot.setReferenceMap(referenceMap);
            changeSnapshot.setChangeValues(stateToChangeValue());
            changeSnapshot.setVersion(currentVersion);
            changeSnapshot.setEntityClass(entityClass.id());
            changeSnapshot.setCreateTime(new DateTimeValue(null, LocalDateTime.now()).valueToLong());
            return Optional.of(new SnapshotEvent(changeSnapshot));
        } else {
            return Optional.empty();
        }
    }

    private List<ChangeValue> stateToChangeValue() {
        IEntityValue entityValue = entityDomain.getEntity().entityValue();
        if (entityValue != null) {
            return entityValue.values().stream().map(x -> {
                ChangeValue changeValue = new ChangeValue();
                changeValue.setFieldId(x.getField().id());
                changeValue.setFieldCode(x.getField().name());
                changeValue.setOp(ChangeValue.Op.SET);
                changeValue.setRawValue(ChangelogHelper.serialize(x));
                return changeValue;
            }).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    /**
     * every arrived change event will produce a version event
     *
     * @param changedEvent
     * @return
     */
    private VersionEvent genVersionEvent(ChangedEvent changedEvent) {
        ChangeVersion changeVersion = new ChangeVersion();
        changeVersion.setTimestamp(changedEvent.getTimestamp());
        changeVersion.setVersion(changedEvent.getCommitId());
        changeVersion.setComment(changedEvent.getComment());
        changeVersion.setId(entityDomain.getId());
        changeVersion.setSource(changedEvent.getId());
        changeVersion.setUsername(changedEvent.getUsername());
        return new VersionEvent(id, changeVersion);
    }

    /**
     * TODO if value not changed will not deliver new event
     * find all related relation to propagation
     *
     * @return
     */
    private List<PropagationChangelogEvent> genPropagationEvent(ChangedEvent changedEvent, Map<String, Object> context) {
        Map<Long, ValueWrapper> valueMap = changedEvent.getValueMap();

        /**
         * find event to propagation
         */
        List<OqsRelation> propagationRelation = EntityClassHelper.findPropagationRelation(entityClass);
        List<OqsRelation> nextRelation = EntityClassHelper.findNextRelation(entityClass);

        /**
         * find event to
         */
        List<Tuple2<OqsRelation, OqsRelation>> associatedRelations = EntityClassHelper.findAssociatedRelations(entityClass);

        Stream<Tuple2<Long, Long>> previewIdsStream = propagationRelation.stream().flatMap(x -> {
            return findRelatedObjIds(x, associatedRelations, valueMap).stream();
        });

        Stream<Tuple2<Long, Long>> nextIdsStream = nextRelation.stream().flatMap(x -> {
            return findNextObjIds(x, valueMap).stream();
        });

        return Stream.concat(previewIdsStream, nextIdsStream).distinct()
                .map(x -> new PropagationChangelogEvent(x._1, x._2, changedEvent, context))
                .collect(Collectors.toList());
    }

    /**
     * find next obj Id
     *
     * @param oqsRelation
     * @param valueMap
     * @return
     */
    private List<Tuple2<Long, Long>> findNextObjIds(OqsRelation oqsRelation, Map<Long, ValueWrapper> valueMap) {

        List<Long> nextObjIds = new LinkedList<>();
        long entityClass = EntityClassHelper.findIdAssociatedEntityClassId(oqsRelation);
        long id = oqsRelation.getEntityField().id();
        Optional<IValue> currentValue = entityDomain.getEntity().entityValue().getValue(id);
        ValueWrapper nextValue = valueMap.get(id);

        currentValue.ifPresent(iValue -> nextObjIds.add(iValue.valueToLong()));

        if (nextValue != null) {
            nextObjIds.add(nextValue.valueToLong());
        }

        return nextObjIds.stream().map(x -> Tuple.of(x, entityClass)).collect(Collectors.toList());
    }

    /**
     * TODO
     * TODO value map  should considered
     * find related id in current value and change event value
     *
     * @param oqsRelation
     * @param associatedRelations
     * @param valueMap
     * @return
     */
    private List<Tuple2<Long, Long>> findRelatedObjIds(OqsRelation oqsRelation
            , List<Tuple2<OqsRelation, OqsRelation>> associatedRelations
            , Map<Long, ValueWrapper> valueMap) {
        //find value has two method
        // 1 -> if this oqsRelation has a associate relation find the associate
        // 2 -> all find in map
        Optional<Tuple2<OqsRelation, OqsRelation>> associatedOqsRelation = associatedRelations.stream()
                .filter(x -> x._1().equals(oqsRelation)).findAny();
        if (associatedOqsRelation.isPresent()) {
            //find in self
            // what is this id's entityClassID
            long id = associatedOqsRelation.get()._2().getEntityField().id();
            Optional<IValue> currentIdValue = entityDomain.getEntity().entityValue().getValue(id);
            ValueWrapper nextIdValue = valueMap.get(id);

            boolean hasOldValue = currentIdValue.isPresent();
            boolean hasNextValue = nextIdValue != null;
            Long associatedEntityClassId = EntityClassHelper
                    .findIdAssociatedEntityClassId(associatedOqsRelation.get()._2());
            if (hasOldValue && hasNextValue) {
                long currentId = currentIdValue.get().valueToLong();
                long nextId = nextIdValue.valueToLong();

                if (currentId != nextId) {
                    return Arrays.asList(Tuple.of(currentId, associatedEntityClassId), Tuple.of(nextId, associatedEntityClassId));
                } else {
                    return Collections.singletonList(Tuple.of(currentId, associatedEntityClassId));
                }
            } else if (!hasOldValue && hasNextValue) {
                long nextId = nextIdValue.valueToLong();
                return Collections.singletonList(Tuple.of(nextId, associatedEntityClassId));

            } else if (hasOldValue) {
                long currentId = currentIdValue.get().valueToLong();
                return Collections.singletonList(Tuple.of(currentId, associatedEntityClassId));
            }

        } else {
            Long associatedEntityClassId = EntityClassHelper
                    .findIdAssociatedEntityClassId(oqsRelation);
            return Optional.ofNullable(entityDomain.getReferenceMap().get(oqsRelation)).map(x -> x.stream().map(id -> {
                return Tuple.of(id, associatedEntityClassId);
            }).collect(Collectors.toList())).orElse(Collections.emptyList());
        }

        return Collections.emptyList();
    }

    /**
     * changedEvent to changelog
     *
     * @param changedEvent
     * @return
     */
    private Changelog toRawChangelog(ChangedEvent changedEvent) {
        Changelog changelog = new Changelog();
        changelog.setId(entityDomain.getId());
        changelog.setEntityClass(entityDomain.getEntity().entityClassRef().getId());
        changelog.setVersion(changedEvent.getCommitId());
        changelog.setUsername(changedEvent.getUsername());
        changelog.setComment(changedEvent.getComment());
        changelog.setCreateTime(changedEvent.getTimestamp());
        return changelog;
    }

    private List<Long> getCurrentRelatedValue(Map<OqsRelation, OqsRelation> mapping, OqsRelation oqsRelation) {
        if (mapping.get(oqsRelation) != null) {
            //find in current
            long id = mapping.get(oqsRelation).getEntityField().id();
            Optional<IValue> value = entityDomain.getEntity().entityValue().getValue(id);
            return value.map(iValue -> Collections.singletonList(iValue.valueToLong())).orElse(Collections.emptyList());
        } else {
            //find in reference map
            return Optional.ofNullable(entityDomain.getReferenceMap().get(oqsRelation)).orElse(Collections.emptyList());
        }
    }

    private Long getOqsRelationRelatedId(Map<OqsRelation, OqsRelation> mapping, OqsRelation oqsRelation) {
        if (mapping.get(oqsRelation) != null) {
            //find in current
            long id = mapping.get(oqsRelation).getEntityField().id();
            return id;
        } else {
            //find in reference map
            return oqsRelation.getEntityField().id();
        }
    }

    /**
     * TODO side effect
     * should consider the OneToOne and ManyToOne
     *
     * @param changedEvent
     * @return
     */
    private Optional<PersistentEvent> updateRelation(ChangedEvent changedEvent) {
        long entityClassId = changedEvent.getEntityClassId();

        Optional<IEntityClass> changeRelatedEntityOp = metaManager.load(entityClassId);
        if (changeRelatedEntityOp.isPresent()) {
            IEntityClass relatedEntity = changeRelatedEntityOp.get();

            List<ChangeValue> changeValues = new LinkedList<>();

            List<Tuple2<OqsRelation, OqsRelation>> associatedRelations = EntityClassHelper.findAssociatedRelations(relatedEntity);

            Map<OqsRelation, OqsRelation> mapping = associatedRelations.stream()
                    .collect(Collectors.toMap(Tuple2::_1, Tuple2::_2, (a, b) -> a));

            //amend relation many to one and
            relatedEntity.oqsRelations().stream()
                    .filter(x -> x.getRightEntityClassId() == entityClass.id())
                    .forEach(oqsRelation -> {
                        if (oqsRelation.getRelationType() == OqsRelation.RelationType.ONE_TO_ONE) {
                            ValueWrapper iValue = changedEvent.getValueMap().get(oqsRelation.getEntityField().id());
                            if (iValue != null && iValue.valueToLong() != entityDomain.getId()) {
                                ChangeValue changeValue = new ChangeValue();
                                changeValue.setRawValue(null);
                                changeValue.setReferenceSet(false);
                                changeValue.setOp(ChangeValue.Op.SET);
                                changeValue.setFieldId(getOqsRelationRelatedId(mapping, oqsRelation));
                                changeValues.add(changeValue);
                            } else if (iValue != null && iValue.valueToLong() == entityDomain.getId()) {
                                List<Long> currentRelatedValue = getCurrentRelatedValue(mapping, oqsRelation);
                                if (!currentRelatedValue.contains(iValue.valueToLong())) {
                                    ChangeValue changeValue = new ChangeValue();
                                    /**
                                     * value is opposite
                                     */
                                    changeValue.setRawValue(Long.toString(changedEvent.getId()));
                                    changeValue.setReferenceSet(false);
                                    changeValue.setOp(ChangeValue.Op.SET);
                                    changeValue.setFieldId(getOqsRelationRelatedId(mapping, oqsRelation));
                                    changeValues.add(changeValue);
                                }
                            }
                        } else if (oqsRelation.getRelationType() == OqsRelation.RelationType.MANY_TO_ONE) {

                            List<Long> currentValues = getCurrentRelatedValue(mapping, oqsRelation);
                            //TODO find related value
                            ValueWrapper iValue = changedEvent.getValueMap().get(oqsRelation.getEntityField().id());
                            if (iValue != null) {
                                //if relation value changed
                                long currentIdValue = iValue.valueToLong();
                                long entityId = changedEvent.getId();

                                boolean alreadyHasRelatedValue = currentValues.contains(entityId);
                                boolean currentIsRelated = (currentIdValue == entityDomain.getId());
                                if (!alreadyHasRelatedValue && currentIsRelated) {
                                    ChangeValue changeValue = new ChangeValue();
                                    changeValue.setFieldId(getOqsRelationRelatedId(mapping, oqsRelation));
                                    changeValue.setReferenceSet(true);
                                    changeValue.setRawValue(Long.toString(entityId));
                                    changeValue.setOp(ChangeValue.Op.ADD);
                                    currentValues.add(entityId);
                                    changeValues.add(changeValue);
                                } else if (alreadyHasRelatedValue && !currentIsRelated) {
                                    ChangeValue changeValue = new ChangeValue();
                                    changeValue.setFieldId(getOqsRelationRelatedId(mapping, oqsRelation));
                                    changeValue.setReferenceSet(true);
                                    changeValue.setRawValue(Long.toString(entityId));
                                    changeValue.setOp(ChangeValue.Op.DEL);
                                    currentValues.add(entityId);
                                    changeValues.add(changeValue);
                                }
                            }
                        } else if (oqsRelation.getRelationType() == OqsRelation.RelationType.ONE_TO_MANY) {
                            //do nothing now
                        }
                    });

            /**
             * do current state update
             */
            updateEntityDomainRelation(changeValues);

            if (changeValues.isEmpty()) {
                return Optional.empty();
            } else {
                Changelog changelog = toRawChangelog(changedEvent);
                changelog.setChangeValues(changeValues);

                //also update internal state
                return Optional.of(new PersistentEvent(changelog));
            }
        }

        return Optional.empty();
    }

    //should update referenceMap
    private void updateEntityDomainState(List<ChangeValue> changeValues) {
        changeValues.forEach(x -> {
            IEntityValue entityValue = entityDomain.getEntity().entityValue();
            if(entityValue == null){
                entityValue = new EntityValue();
                entityDomain.getEntity().resetEntityValue(entityValue);
            }
            Optional<IValue> value = entityValue.getValue(x.getFieldId());
            if (value.isPresent()) {
                //has old value
                //TODO copy value
                IValue iValue = value.get().copy();
                iValue.setStringValue(x.getRawValue());
                entityValue.addValue(iValue);
            } else {
                Optional<IEntityField> field = entityClass.field(x.getFieldId());
                if (field.isPresent()) {
                    IEntityField targetField = field.get();
                    Optional<IValue> iValue = targetField.type().toTypedValue(targetField, x.getRawValue());
                    iValue.ifPresent(entityValue::addValue);
                }
            }
        });
    }

    /**
     * TODO make this a common method
     * replay with no changeValues
     *
     * @param changeValues
     */
    private void updateEntityDomainRelation(List<ChangeValue> changeValues) {
        Map<Long, List<ChangeValue>> idChangesMapping = changeValues.stream()
                .collect(Collectors.groupingBy(ChangeValue::getFieldId));

        Map<OqsRelation, List<Long>> referenceMap = entityDomain.getReferenceMap();
        idChangesMapping.forEach((a, b) -> {
            Optional<Map.Entry<OqsRelation, List<Long>>> related = referenceMap
                    .entrySet().stream().filter(x -> x.getKey().getEntityField().id() == a).findFirst();
            if (related.isPresent()) {

                List<Long> value = related.get().getValue();
                if (value == null) {
                    value = new ArrayList<>();
                }

                List<Long> finalValue = value;

                //do changelogs
                b.forEach(changeValue -> {
                    ChangeValue.Op op = changeValue.getOp();
                    String rawValue = changeValue.getRawValue();

                    switch (op) {
                        case SET:
                            if (finalValue.isEmpty() && rawValue != null) {
                                finalValue.add(Long.parseLong(rawValue));
                            }

                            if (!finalValue.isEmpty()) {
                                finalValue.set(0, rawValue == null ? null : Long.parseLong(rawValue));
                            }
                            break;
                        case ADD:
                            if (rawValue != null) {
                                finalValue.add(Long.parseLong(rawValue));
                            }
                            break;
                        case DEL:
                            if (rawValue != null) {
                                finalValue.remove(Long.parseLong(rawValue));
                            }
                            break;
                        default:
                    }
                });
            }
        });
    }

    /**
     * TODO side effect
     *
     * @param changedEvent
     * @return
     */
    private Optional<PersistentEvent> updateSelf(ChangedEvent changedEvent) {
        /**
         * full new state
         */
        Map<Long, ValueWrapper> after = changedEvent.getValueMap();
        IEntityValue entityValue = entityDomain.getEntity().entityValue();
        if (entityValue != null) {
            Map<Long, IValue> before = entityValue.values().stream().collect(Collectors.toMap(x -> x.getField().id(), y -> y));
            List<ChangeValue> selfResult = compareSelf(before, entityDomain.getReferenceMap(), after);

            Changelog changelog = new Changelog();
            changelog.setComment(changedEvent.getComment());
            changelog.setUsername(changedEvent.getUsername());
            changelog.setCreateTime(changedEvent.getTimestamp());
            changelog.setVersion(changedEvent.getCommitId());
            changelog.setEntityClass(changedEvent.getEntityClassId());
            changelog.setId(changedEvent.getId());
            changelog.setChangeValues(selfResult);

            updateEntityDomainState(selfResult);
            updateEntityDomainRelation(selfResult);
            return Optional.of(new PersistentEvent(changelog));
        } else {
            //TODO new empty value
            Changelog changelog = new Changelog();
            changelog.setComment(changedEvent.getComment());
            changelog.setUsername(changedEvent.getUsername());
            changelog.setCreateTime(changedEvent.getTimestamp());
            changelog.setVersion(changedEvent.getCommitId());
            changelog.setEntityClass(changedEvent.getEntityClassId());
            changelog.setId(changedEvent.getId());

            List<ChangeValue> changeValues = genNewChangeValue(after);
            changelog.setChangeValues(changeValues);

            updateEntityDomainState(changeValues);
            updateEntityDomainRelation(changeValues);

            return Optional.of(new PersistentEvent(changelog));
        }
    }

    private List<ChangeValue> genNewChangeValue(Map<Long, ValueWrapper> values) {

        List<ChangeValue> changeValues = new ArrayList<>();

        values.forEach((key, value) -> {
            ChangeValue cv = new ChangeValue();
            cv.setRawValue(value == null ? null : ChangelogHelper.serialize(value));
            cv.setOp(ChangeValue.Op.SET);
            cv.setFieldId(key);
            cv.setReferenceSet(false);
            changeValues.add(cv);
        });

        return changeValues;
    }

    /**
     * TODO update this
     * update the internalState
     */
    synchronized private Optional<PersistentEvent> updateInternalState(ChangedEvent changedEvent) {

        /**
         * changeEvent owner id
         */
        long changeEventOwnerId = changedEvent.getId();

        /**
         * update common state
         */
        updateCommonState(changedEvent);

        if (changeEventOwnerId != entityDomain.getId()) {
            //deal the relation
            return updateRelation(changedEvent);
        } else {
            /**
             * will gen related event and update relation internal
             */
            return updateSelf(changedEvent);
        }
    }

    private void updateCommonState(ChangedEvent changedEvent) {
        long newVersion = changedEvent.getCommitId();
        this.currentVersion = newVersion;
        this.count++;
    }

    /**
     * after will effect the referenceMap
     * compare  before + referenceMap with after
     *
     * @param before
     * @param referenceMap
     * @param after
     * @return
     */
    private List<ChangeValue> compareSelf(Map<Long, IValue> before, Map<OqsRelation, List<Long>> referenceMap, Map<Long, ValueWrapper> after) {
        List<ChangeValue> changeValues = new ArrayList<>();

        Set<Long> beforeIds = before.keySet();
        Set<Long> afterIds = after.keySet();
        Map<Long, IValue> referenceMapWithIValue = new HashMap<>();
        referenceMap.entrySet()
                .stream()
                .filter(x -> {
                    OqsRelation rel = x.getKey();
                    return rel.getLeftEntityClassId() == entityClass.id()
                            && rel.getRelationType() != OqsRelation.RelationType.ONE_TO_MANY;
                }).forEach(entry -> {
            OqsRelation key = entry.getKey();
            List<Long> value = entry.getValue();
            if (value != null) {
                referenceMapWithIValue.put(key.getEntityField().id(), new LongValue(key.getEntityField(), value.get(0)));
            } else {
                referenceMapWithIValue.put(key.getEntityField().id(), null);
            }
        });

        Set<Long> referenceIdSet = referenceMapWithIValue.keySet();

        Sets.union(referenceIdSet, Sets.union(beforeIds, afterIds)).forEach(id -> {

            IValue beforeValue = before.get(id);

            if(beforeValue == null){
                beforeValue = referenceMapWithIValue.get(id);
            }

            ValueWrapper afterValue;
            if (after.containsKey(id)) {
                afterValue = after.get(id);
            } else {
                // no contains
                return;
            }

            ChangeValue changeValue = new ChangeValue();
            changeValue.setFieldId(id);
//            if (afterValue != null
//                    && afterValue.getField() != null
//                    && afterValue.getField().name() != null) {
//                changeValue.setFieldCode(afterValue.getField().name());
//            }

            if (beforeValue == null) {
                //TODO ADD?
                changeValue.setOp(ChangeValue.Op.SET);
                changeValue.setRawValue(ChangelogHelper.serialize(afterValue));
                changeValues.add(changeValue);
            } else if (afterValue == null) {
                //TODO DEL?
                changeValue.setOp(ChangeValue.Op.SET);
                changeValues.add(changeValue);
            } else {
                if (!afterValue.equals(beforeValue)) {
                    //TODO check IValue if is equals
                    changeValue.setOp(ChangeValue.Op.SET);
                    changeValue.setRawValue(ChangelogHelper.serialize(afterValue));
                }
                changeValues.add(changeValue);
            }
        });
        return changeValues;
    }

    @Override
    public String toString() {
        return "ChangelogStatefulEntity{" +
                "count=" + count +
                ", currentVersion=" + currentVersion +
                ", id=" + id +
                ", entityDomain=" + entityDomain +
                ", entityClass=" + entityClass +
                ", metaManager=" + metaManager +
                ", snapshotThreshold=" + snapshotThreshold +
                '}';
    }
}
