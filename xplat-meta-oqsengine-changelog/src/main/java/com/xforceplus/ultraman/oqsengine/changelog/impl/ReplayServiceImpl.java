package com.xforceplus.ultraman.oqsengine.changelog.impl;

import com.xforceplus.ultraman.oqsengine.changelog.ReplayService;
import com.xforceplus.ultraman.oqsengine.changelog.domain.*;
import com.xforceplus.ultraman.oqsengine.changelog.storage.ChangelogStorage;
import com.xforceplus.ultraman.oqsengine.changelog.utils.ChangelogHelper;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.oqs.OqsRelation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.*;

import static com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeValue.Op.SET;
import static com.xforceplus.ultraman.oqsengine.changelog.utils.ChangelogHelper.deserialize;
import static com.xforceplus.ultraman.oqsengine.changelog.utils.ChangelogHelper.isReferenceSet;

/**
 * rebuild from changlog
 */
public class ReplayServiceImpl implements ReplayService {

    private Logger logger = LoggerFactory.getLogger(ReplayService.class);

    @Resource
    private ChangelogStorage changelogStorage;

    @Resource
    private MetaManager metaManager;

    /**
     * find all related changelog
     *
     * @param id
     * @return
     */
    @Override
    public List<Changelog> getRelatedChangelog(long id) {
        return changelogStorage.findById(id, -1);
    }

    /**
     * TODO consider the snapshot
     * @param id
     * @param version
     * @return
     */
    @Override
    public List<Changelog> getRelatedChangelog(long id, long version) {
        return changelogStorage.findById(id, version);
    }


    /**
     * side effect
     *
     * @param historyValues
     */
    private void linkAllHistory(List<HistoryValue> historyValues) {
        historyValues.stream().reduce((a, b) -> {
            a.setNext(b);
            b.setPreview(a);
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
        Map<OqsRelation, Collection<ValueLife>> relationValues = new HashMap<>();

        entityRelation.setRelatedIds(relationValues);

        Map<Long, Map<String, ValueLife>> valueMap = new HashMap<>();

        /**
         * deal every relations to find out history and current value and record in relationValues
         */
        entityClass.oqsRelations().stream()
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
                            Map<String, ValueLife> valueLifeMap = valueMap.get(x.getEntityField().id());
                            if (valueLifeMap == null) {
                                valueLifeMap = new HashMap<>();
                                valueMap.put(id, valueLifeMap);
                            }

                            ValueLife valueLife = valueLifeMap.get(value.getRawValue());

                            /**
                             * SET |  (ADD | DEL) will not mix with each other
                             */
                            switch (value.getOp()) {
                                case ADD:
                                    valueLife.setStart(historyValue.getCommitId());
                                    break;
                                case DEL:
                                    valueLife.setEnd(historyValue.getCommitId());
                                    break;
                                case SET:
                                    valueLife.setStart(historyValue.getCommitId());
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
     * @param entityClass
     * @param id
     * @param version
     * @return
     */
    @Override
    public EntityAggDomain replayDomain(long entityClass, long id, long version) {

        Queue<Tuple3<Long, Long, Long>> taskQueue = new LinkedList<>();

        Map<Long, EntityDomain> footprint = new HashMap<>();

        taskQueue.offer(Tuple.of(entityClass, id, version));
        while (!taskQueue.isEmpty()){
            Tuple3<Long, Long, Long> task = taskQueue.poll();

            Optional<IEntityClass> entityClassOptional = metaManager.load(task._1);
            if(entityClassOptional.isPresent()) {
                List<Changelog> relatedChangelog = this.getRelatedChangelog(task._2, task._3);
                EntityDomain entityDomain = replaySingleDomain(entityClassOptional.get()
                        , id, relatedChangelog);
                footprint.put(id, entityDomain);
                entityDomain.getReferenceMap().forEach((key, value) -> {
                    //put in
                    value.forEach(eachId -> {
                        taskQueue.offer(Tuple.of(key.getEntityClassId(),eachId, version));
                    });
                });
            }
        }
        EntityAggDomain head = new EntityAggDomain();
        buildAggDomain(id, footprint, head);
        return head;
    }

    private void buildAggDomain(long id, Map<Long, EntityDomain> map, EntityAggDomain head){

        /**
         * current id, currentDomain, preview AggDomain
         */
        Queue<Tuple3<Long, EntityAggDomain, Tuple2<OqsRelation, EntityAggDomain>>> taskIdQueue = new LinkedList<>();

        taskIdQueue.offer(Tuple.of(id, head, null));
        //BFS
        while(!taskIdQueue.isEmpty()){
            Tuple3<Long, EntityAggDomain, Tuple2<OqsRelation, EntityAggDomain>> task = taskIdQueue.poll();

            Long currentNodeId = task._1;
            EntityAggDomain currentEntityAgg = task._2;
            Tuple2<OqsRelation, EntityAggDomain> previewEntityRelToAgg = task._3;

            EntityDomain entityDomain = map.get(currentNodeId);
            if(entityDomain != null){
                currentEntityAgg.setRootIEntity(entityDomain.getEntity());
                if(previewEntityRelToAgg != null){
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

    private IValue getValue(List<ChangeValue> changeValues, IEntityField field){
        Optional<ChangeValue> firstSet = changeValues.stream().filter(x -> x.getOp() == SET).findFirst();
        if(firstSet.isPresent()){
            ChangeValue changeValue = firstSet.get();
            String rawValue = changeValue.getRawValue();
            IValue value = deserialize(rawValue, field);
            return value;
        }

        return null;
    }

    /**
     * replay the changelogs to ane entityDomain
     * @param entityClass
     * @param id
     * @param changelogs
     * @return
     */
    private EntityDomain replaySingleDomain(IEntityClass entityClass, long id, List<Changelog> changelogs){
        EntityDomain entityDomain = new EntityDomain();


        EntityValue entityValue = new EntityValue(id);
        Map<OqsRelation, List<Long>> referenceMap = new HashMap<>();

        Map<Long, List<ChangeValue>> mappedValue = ChangelogHelper.getMappedValue(changelogs);

        /**
         * check every field current value
         */
        entityClass.fields().forEach(field -> {
            //TODO check order
            List<ChangeValue> changeValues = mappedValue.get(field.id());
            IValue value = getValue(changeValues, field);
            if(value != null){
                entityValue.addValue(value);
            }
        });

        /**
         * deal relation value
         */
        entityClass.oqsRelations().forEach(rel -> {
            if(rel.getRelOwnerClassId() == entityClass.id()){
                List<ChangeValue> changeValues = mappedValue.get(rel.getEntityField().id());
                if(isReferenceSet(rel)){
                    List<Long> ids = new LinkedList<>();
                    changeValues.forEach(changeValue -> {
                        switch(changeValue.getOp()) {
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

                    if(!ids.isEmpty()) {
                        referenceMap.put(rel, ids);
                    }
                } else {
                    IValue value = getValue(changeValues, rel.getEntityField());
                    if(value != null){
                        referenceMap.put(rel, Collections.singletonList(value.valueToLong()));
                    }
                }
            }
        });

        return entityDomain;
    }
}
