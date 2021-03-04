package com.xforceplus.ultraman.oqsengine.changelog.impl;

import com.xforceplus.ultraman.oqsengine.changelog.ChangelogService;
import com.xforceplus.ultraman.oqsengine.changelog.ReplayService;
import com.xforceplus.ultraman.oqsengine.changelog.domain.*;
import com.xforceplus.ultraman.oqsengine.changelog.relation.RelationAwareChangelog;
import com.xforceplus.ultraman.oqsengine.changelog.storage.ChangelogStorage;
import com.xforceplus.ultraman.oqsengine.common.id.IdGenerator;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.oqs.OqsRelation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.changelog.utils.ChangelogHelper.serialize;

/**
 * default changelog implementation
 */
public class DefaultChangelogImpl implements ChangelogService {

    @Autowired
    private List<RelationAwareChangelog> relationAwareChangeLogs;

    @Resource
    private MetaManager metaManager;

    @Resource
    private ChangelogStorage changelogStorage;

    @Resource
    private ReplayService replayService;

    @Resource(name = "snowflakeIdGenerator")
    private IdGenerator<Long> idGenerator;

    private Logger logger = LoggerFactory.getLogger(ChangelogService.class);

    private String FATAL_ERR = "Fatal Err: got non-exists entityClass:%s's changelog";

    /**
     * changed event to generate Changelogs
     * changelog should always contains relation value with full value
     * @param changedEvent
     * @return
     */
    @Override
    public List<Changelog> generateChangeLog(ChangedEvent changedEvent) {

        List<Changelog> changeLogs = new LinkedList<>();

        long entityClassId = changedEvent.getEntityClassId();

        /**
         * get main entityClass
         */
        IEntityClass entityClass = metaManager.load(entityClassId)
                .orElseThrow(() -> new RuntimeException(String.format(FATAL_ERR, entityClassId)));

        List<Changelog> sourceChangelog = handleEvent(changedEvent, entityClass, null);

        changeLogs.addAll(sourceChangelog);
        List<Changelog> records = entityClass.oqsRelations().stream().filter(x -> x.isCompanion()).flatMap(x -> {
            /**
             * entityclass is self
             */
            return handleEvent(changedEvent, x.getEntityClass(), x).stream();
        }).collect(Collectors.toList());

        changeLogs.addAll(records);
        return changeLogs;
    }


    /**
     * genChangelog when a changedEvent comes, depend on which relation
     *
     * @param changedEvent
     * @param entityClass
     * @return
     */
    private List<Changelog> handleEvent(ChangedEvent changedEvent, IEntityClass entityClass, OqsRelation oqsRelation) {
        if (entityClass.id() == changedEvent.getEntityClassId() && oqsRelation == null) {
            return Collections.singletonList(genSourceChangelog(changedEvent));
        } else {
            List<Changelog> changelogs = relationAwareChangeLogs.stream().filter(x -> x.require(oqsRelation))
                    .flatMap(x -> x.generateOuterChangelog(oqsRelation, entityClass, changedEvent).stream())
                    .collect(Collectors.toList());

            return changelogs;
        }
    }

    /**
     * change log
     *
     * @param changedEvent
     * @return
     */
    private Changelog genSourceChangelog(ChangedEvent changedEvent) {

        IEntity afterEntity = changedEvent.getAfter();
        String comment = changedEvent.getComment();
        long timestamp = changedEvent.getTimestamp();
        long commitId = changedEvent.getCommitId();
        long entityClassId = changedEvent.getEntityClassId();

        Changelog changelog = new Changelog();

        //TODO
        switch (changedEvent.getOperationType()) {
            case CREATE:
            case UPDATE:
                List<ChangeValue> changeValues = toChangeValue(afterEntity);
                changelog.setcId(idGenerator.next());
                changelog.setComment(comment);
                changelog.setCreateTime(timestamp);
                changelog.setEntityClass(entityClassId);
                changelog.setChangeValues(changeValues);
                changelog.setId(afterEntity.id());
                changelog.setVersion(commitId);
                break;
            case DELETE:
                changelog.setcId(idGenerator.next());
                changelog.setComment(comment);
                changelog.setCreateTime(timestamp);
                changelog.setEntityClass(entityClassId);
                changelog.setChangeValues(Collections.emptyList());
                changelog.setId(afterEntity.id());
                changelog.setVersion(commitId);
                changelog.setDeleteFlag(1);
                break;
            default:
                changelog = null;
        }

        return changelog;
    }

    @Override
    public void saveChangeLogs(List<Changelog> changeLogs) {
        changelogStorage.saveBatch(changeLogs);
    }

    @Override
    public List<ChangeVersion> getChangeLog(long objId, long entityClassId) {

        List<ChangeVersion> changeVersionList = new ArrayList<>();

        Stack<VersiondEntityRef> stack = new Stack<>();

        stack.push(new VersiondEntityRef(entityClassId, objId));

        while(!stack.isEmpty()){
            VersiondEntityRef nextNode = stack.pop();
            findChangeVersion(changeVersionList, nextNode.getId(), nextNode.getVersion(), nextNode.getEntityClassId(), stack);
        }

        return changeVersionList;
    }

    private void findChangeVersion(List<ChangeVersion> changeVersionList, long objId, long version, long entityClassId, Stack<VersiondEntityRef> stack){
        List<Changelog> relatedChangelog = replayService.getRelatedChangelog(objId, version);
        if(!relatedChangelog.isEmpty()){

            //add current version
            changeVersionList.addAll(relatedChangelog.stream().map(x -> {
                ChangeVersion changeVersion = new ChangeVersion();
                changeVersion.setComment(x.getComment());
                changeVersion.setVersion(Long.toString(x.getVersion()));
                return changeVersion;
            }).collect(Collectors.toList()));

            Optional<IEntityClass> entityClassOp = metaManager.load(entityClassId);
            if(entityClassOp.isPresent()) {
                IEntityClass entityClass = entityClassOp.get();
                EntityRelation mainRelation = replayService.replayRelation(entityClass, objId, relatedChangelog);
                //search next values
                mainRelation.getRelatedIds().forEach((key, value) -> {
                    long childEntity = key.getEntityClassId();
                    value.forEach(valueLife -> {
                        String idValue = valueLife.getValue();
                        try {
                            long idLong = Long.parseLong(idValue);
                            long endVersion = valueLife.getEnd();
                            stack.push(new VersiondEntityRef(childEntity, idLong, endVersion));
                        }catch(Exception ex){
                            //TODO
                            logger.error("{}", ex);
                        }
                    });
                });
            }
        }
    }

    /**
     * TODO a new structure to represent
     * @param entityClass
     * @param objId
     * @param version
     * @return
     */
    @Override
    public EntityAggDomain replayEntity(long entityClass, long objId, long version) {
        return replayService.replayDomain(entityClass, objId, version);
    }

    //TODO to consider the entity change
    private List<ChangeValue> toChangeValue(IEntity entity) {

        IEntityValue entityValue = entity.entityValue();
        Collection<IValue> values = entityValue.values();

        List<ChangeValue> changeValues = values.stream().map(x -> {
            ChangeValue changeValue = new ChangeValue();
            changeValue.setFieldId(x.getField().id());
            changeValue.setFieldCode(x.getField().name());
            changeValue.setOp(ChangeValue.Op.SET);
            changeValue.setRawValue(serialize(x));
            return changeValue;
        }).collect(Collectors.toList());

        return changeValues;
    }
}
