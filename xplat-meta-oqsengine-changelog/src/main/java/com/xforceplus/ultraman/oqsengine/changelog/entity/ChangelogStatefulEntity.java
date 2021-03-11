package com.xforceplus.ultraman.oqsengine.changelog.entity;

import com.google.common.collect.Sets;
import com.xforceplus.ultraman.oqsengine.changelog.command.AddChangelog;
import com.xforceplus.ultraman.oqsengine.changelog.command.ChangelogCommand;
import com.xforceplus.ultraman.oqsengine.changelog.domain.*;
import com.xforceplus.ultraman.oqsengine.changelog.event.ChangelogEvent;
import com.xforceplus.ultraman.oqsengine.changelog.event.PersistentEvent;
import com.xforceplus.ultraman.oqsengine.changelog.event.PropagationChangelogEvent;
import com.xforceplus.ultraman.oqsengine.changelog.event.VersionEvent;
import com.xforceplus.ultraman.oqsengine.changelog.utils.ChangelogHelper;
import com.xforceplus.ultraman.oqsengine.changelog.utils.EntityClassHelper;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldLikeRelationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.oqs.OqsRelation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * a stateful entity
 */
public class ChangelogStatefulEntity implements StatefulEntity<EntityDomain, ChangelogCommand, ChangelogEvent> {

    private Logger logger = LoggerFactory.getLogger(ChangelogStatefulEntity.class);

    /**
     * changelog retrieved
     */
    private long count;

    /**
     * main value
     */
    private IEntity entity;

    /**
     * current relation map with all related or be related
     * oqsRelation is not self is related --- a reversed set
     * oqsRelation is self --- a id set
     */
    private Map<OqsRelation, List<Long>> referenceMap = new HashMap<>();

    private IEntityClass entityClass;

    private MetaManager metaManager;

    /**
     * TODO init
     */
    public ChangelogStatefulEntity(IEntityClass entityClass, MetaManager metaManager) {
        this.metaManager = metaManager;
        this.entityClass = entityClass;
    }

    @Override
    public List<ChangelogEvent> receive(ChangelogCommand input) {

        List<ChangelogEvent> retList = new LinkedList<>();
        if (input instanceof AddChangelog) {

            ChangedEvent changedEvent = ((AddChangelog) input).getChangedEvent();
            //deal add changelog
            Optional<PersistentEvent> persistentEvent = updateInternalState(changedEvent);
            //add ret event
            persistentEvent.ifPresent(retList::add);
            retList.addAll(genPropagationEvent(changedEvent));
            retList.add(genVersionEvent(changedEvent));
        } else {
            logger.error("Unknown Command {}", input);
        }
        return retList;
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
        changeVersion.setId(entity.id());
        changeVersion.setSource(changedEvent.getId());
        return new VersionEvent(changeVersion);
    }

    /**
     * TODO if value not changed will not deliver new event
     * find all related relation to propagation
     *
     * @return
     */
    private List<PropagationChangelogEvent> genPropagationEvent(ChangedEvent changedEvent) {
        Map<Long, IValue> valueMap = changedEvent.getValueMap();

        /**
         * find event to propagation
         */
        List<OqsRelation> propagationRelation = EntityClassHelper.findPropagationRelation(entityClass);
        List<OqsRelation> nextRelation = EntityClassHelper.findNextRelation(entityClass);

        /**
         * find event to
         */
        List<Tuple2<OqsRelation, OqsRelation>> associatedRelations = EntityClassHelper.findAssociatedRelations(entityClass);

        Stream<Long> previewIdsStream = propagationRelation.stream().flatMap(x -> {
            return findRelatedObjIds(x, associatedRelations, valueMap).stream();
        });

        Stream<Long> nextIdsStream = nextRelation.stream().flatMap(x -> {
            return findNextObjIds(x, valueMap).stream();
        });

        return Stream.concat(previewIdsStream, nextIdsStream).distinct()
                .map(x -> new PropagationChangelogEvent(x, changedEvent))
                .collect(Collectors.toList());
    }

    /**
     * find next obj Id
     *
     * @param oqsRelation
     * @param valueMap
     * @return
     */
    private List<Long> findNextObjIds(OqsRelation oqsRelation, Map<Long, IValue> valueMap) {

        List<Long> nextObjIds = new LinkedList<>();

        long id = oqsRelation.getEntityField().id();
        Optional<IValue> currentValue = entity.entityValue().getValue(id);
        IValue nextValue = valueMap.get(id);

        currentValue.ifPresent(iValue -> nextObjIds.add(iValue.valueToLong()));

        if (nextValue != null) {
            nextObjIds.add(nextValue.valueToLong());
        }

        return nextObjIds;
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
    private List<Long> findRelatedObjIds(OqsRelation oqsRelation, List<Tuple2<OqsRelation, OqsRelation>> associatedRelations, Map<Long, IValue> valueMap) {
        //find value has two method
        // 1 -> if this oqsRelation has a associate relation find the associate
        // 2 -> all find in map
        Optional<Tuple2<OqsRelation, OqsRelation>> associatedOqsRelation = associatedRelations.stream().filter(x -> x._1().equals(oqsRelation)).findAny();
        if (associatedOqsRelation.isPresent()) {
            //find in self
            long id = associatedOqsRelation.get()._2().getEntityField().id();
            Optional<IValue> currentIdValue = entity.entityValue().getValue(id);
            IValue nextIdValue = valueMap.get(id);

            boolean hasOldValue = currentIdValue.isPresent();
            boolean hasNextValue = nextIdValue != null;

            if (hasOldValue && hasNextValue) {
                long currentId = currentIdValue.get().valueToLong();
                long nextId = nextIdValue.valueToLong();
                if (currentId != nextId) {
                    return Arrays.asList(currentId, nextId);
                } else {
                    return Collections.singletonList(currentId);
                }
            } else if (!hasOldValue && hasNextValue) {
                long nextId = nextIdValue.valueToLong();
                return Collections.singletonList(nextId);

            } else if(hasOldValue){
                long currentId = currentIdValue.get().valueToLong();
                return Collections.singletonList(currentId);
            }

        } else {
            return Optional.ofNullable(referenceMap.get(oqsRelation)).orElse(Collections.emptyList());

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
        changelog.setId(entity.id());
        changelog.setEntityClass(entity.entityClass().id());
        changelog.setVersion(changedEvent.getCommitId());
        changelog.setUsername(changedEvent.getUsername());
        changelog.setComment(changedEvent.getComment());
        changelog.setCreateTime(changedEvent.getTimestamp());
        return changelog;
    }

    private List<Long> getCurrentRelatedValue(Map<OqsRelation, OqsRelation> mapping, OqsRelation oqsRelation){
        if(mapping.get(oqsRelation) != null){
            //find in current
            long id = mapping.get(oqsRelation).getEntityField().id();
            Optional<IValue> value = entity.entityValue().getValue(id);
            return value.map(iValue -> Collections.singletonList(iValue.valueToLong())).orElse(Collections.emptyList());
        } else {
            //find in reference map
            return Optional.ofNullable(referenceMap.get(oqsRelation)).orElse(Collections.emptyList());
        }
    }

    private Long getOqsRelationRelatedId(Map<OqsRelation, OqsRelation> mapping, OqsRelation oqsRelation){
        if(mapping.get(oqsRelation) != null){
            //find in current
            long id = mapping.get(oqsRelation).getEntityField().id();
            return id;
        } else {
            //find in reference map
            return oqsRelation.getEntityField().id();
        }
    }

    /**
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
                    .filter(x -> x.getEntityClassId() == entityClass.id())
                    .forEach(oqsRelation -> {
                        if(oqsRelation.getRelationType().equalsIgnoreCase(FieldLikeRelationType.ONE2ONE.getName())){
                            IValue iValue = changedEvent.getValueMap().get(oqsRelation.getEntityField().id());
                            if(iValue != null && iValue.valueToLong() != entity.id()){
                                ChangeValue changeValue = new ChangeValue();
                                changeValue.setRawValue(null);
                                changeValue.setReferenceSet(false);
                                changeValue.setOp(ChangeValue.Op.SET);
                                changeValue.setFieldId(getOqsRelationRelatedId(mapping, oqsRelation));
                                changeValues.add(changeValue);
                            } else if(iValue != null && iValue.valueToLong() == entity.id()){
                                List<Long> currentRelatedValue = getCurrentRelatedValue(mapping, oqsRelation);
                                if(!currentRelatedValue.contains(iValue.valueToLong())){
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
                        }else if(oqsRelation.getRelationType().equalsIgnoreCase(FieldLikeRelationType.MANY2ONE.getName())){

                            List<Long> currentValues = getCurrentRelatedValue(mapping, oqsRelation);
                            //TODO find related value
                            IValue iValue = changedEvent.getValueMap().get(oqsRelation.getEntityField().id());
                            if (iValue != null) {
                                //if relation value changed
                                long currentIdValue = iValue.valueToLong();
                                long entityId = changedEvent.getId();

                                boolean alreadyHasRelatedValue = currentValues.contains(entityId);
                                boolean currentIsRelated = (currentIdValue == entity.id());
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
                        }else if(oqsRelation.getRelationType().equalsIgnoreCase(FieldLikeRelationType.ONE2MANY.getName())){
                            //do nothing now
                        }
                    });

            if (changeValues.isEmpty()) {
                return Optional.empty();
            } else {
                Changelog changelog = toRawChangelog(changedEvent);
                changelog.setChangeValues(changeValues);
                return Optional.of(new PersistentEvent(changelog));
            }
        }

        return Optional.empty();
    }

    private Optional<PersistentEvent> updateSelf(ChangedEvent changedEvent) {
        /**
         * full new state
         */
        Map<Long, IValue> after = changedEvent.getValueMap();
        IEntityValue entityValue = entity.entityValue();
        if (entityValue != null) {
            Map<Long, IValue> before = entityValue.values().stream().collect(Collectors.toMap(x -> x.getField().id(), y -> y));
            List<ChangeValue> selfResult = compareSelf(before, after);

            Changelog changelog = new Changelog();
            changelog.setComment(changedEvent.getComment());
            changelog.setUsername(changedEvent.getUsername());
            changelog.setCreateTime(changedEvent.getTimestamp());
            changelog.setVersion(changedEvent.getCommitId());
            changelog.setEntityClass(changedEvent.getEntityClassId());
            changelog.setId(changedEvent.getId());
            changelog.setChangeValues(selfResult);

            return Optional.of(new PersistentEvent(changelog));
        }

        return Optional.empty();
    }

    /**
     * update the internalState
     */
    private Optional<PersistentEvent> updateInternalState(ChangedEvent changedEvent) {

        long changeEventRelatedId = changedEvent.getId();
        if (changeEventRelatedId != entity.id()) {
            //deal the relation
            return updateRelation(changedEvent);
        } else {
            return updateSelf(changedEvent);
        }
    }

    private List<ChangeValue> compareSelf(Map<Long, IValue> before, Map<Long, IValue> after) {
        List<ChangeValue> changeValues = new ArrayList<>();

        Set<Long> beforeIds = before.keySet();
        Set<Long> afterIds = after.keySet();

        Sets.union(beforeIds, afterIds).forEach(id -> {
            IValue beforeValue = before.get(id);
            IValue afterValue = after.get(id);

            ChangeValue changeValue = new ChangeValue();
            changeValue.setFieldId(id);
            if (afterValue != null
                    && afterValue.getField() != null
                    && afterValue.getField().name() != null) {
                changeValue.setFieldCode(afterValue.getField().name());
            }

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
}
