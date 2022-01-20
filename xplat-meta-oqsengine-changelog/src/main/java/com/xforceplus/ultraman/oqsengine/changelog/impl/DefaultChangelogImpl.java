package com.xforceplus.ultraman.oqsengine.changelog.impl;

import static com.xforceplus.ultraman.oqsengine.changelog.utils.ChangelogHelper.mergeSortedList;
import static com.xforceplus.ultraman.oqsengine.changelog.utils.ChangelogHelper.serialize;

import com.xforceplus.ultraman.oqsengine.changelog.ChangelogService;
import com.xforceplus.ultraman.oqsengine.changelog.ReplayService;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeValue;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeVersion;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangedEvent;
import com.xforceplus.ultraman.oqsengine.changelog.domain.Changelog;
import com.xforceplus.ultraman.oqsengine.changelog.domain.EntityAggDomain;
import com.xforceplus.ultraman.oqsengine.changelog.domain.EntityRelation;
import com.xforceplus.ultraman.oqsengine.changelog.domain.VersiondEntityRef;
import com.xforceplus.ultraman.oqsengine.changelog.relation.RelationAwareChangelog;
import com.xforceplus.ultraman.oqsengine.changelog.storage.write.ChangelogStorage;
import com.xforceplus.ultraman.oqsengine.common.id.IdGenerator;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * default changelog implementation
 */
public class DefaultChangelogImpl implements ChangelogService {

    @Autowired(required = false)
    private List<RelationAwareChangelog> relationAwareChangeLogs;

    @Resource
    private MetaManager metaManager;

    @Resource
    private ChangelogStorage changelogStorage;

    @Resource
    private ReplayService replayService;

    @Resource(name = "longNoContinuousPartialOrderIdGenerator")
    private IdGenerator<Long> idGenerator;

    private Logger logger = LoggerFactory.getLogger(ChangelogService.class);

    private String FATAL_ERR = "Fatal Err: got non-exists entityClass:%s's changelog";

    /**
     * changed event to generate Changelogs
     * changelog should always contains relation value with full value
     */
    @Override
    public Changelog generateChangeLog(IEntityClass entityClass, ChangedEvent changedEvent) {

        List<Changelog> changeLogs = new LinkedList<>();
        long entityClassId = changedEvent.getEntityClassId();

        String profile = entityClass.ref().getProfile();

        /**
         * get main entityClass
         */
        IEntityClass entityClassOp = metaManager.load(entityClassId, profile)
            .orElseThrow(() -> new RuntimeException(String.format(FATAL_ERR, entityClassId)));

        List<Changelog> sourceChangelog = handleEvent(changedEvent, entityClass, null);

        changeLogs.addAll(sourceChangelog);
        List<Changelog> records = entityClass.relationship().stream().filter(x -> x.isCompanion()).flatMap(x -> {
            /**
             * entityclass is self ? TODO
             */
            return handleEvent(changedEvent, x.getRightEntityClass(profile), x).stream();
        }).collect(Collectors.toList());

        changeLogs.addAll(records);
        return null;
    }


    /**
     * genChangelog when a changedEvent comes, depend on which relation
     */
    private List<Changelog> handleEvent(ChangedEvent changedEvent, IEntityClass entityClass,
                                        Relationship relationship) {
        if (entityClass.id() == changedEvent.getEntityClassId() && relationship == null) {
            return Collections.singletonList(genSourceChangelog(changedEvent));
        } else {
            List<Changelog> changelogs = relationAwareChangeLogs.stream().filter(x -> x.require(relationship))
                .flatMap(x -> x.generateOuterChangelog(relationship, entityClass, changedEvent).stream())
                .collect(Collectors.toList());

            return changelogs;
        }
    }

    /**
     * change log
     */
    private Changelog genSourceChangelog(ChangedEvent changedEvent) {

        IEntity afterEntity = null;
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

        List<ChangeVersion> changeVersionList = Collections.emptyList();

        Stack<VersiondEntityRef> stack = new Stack<>();

        stack.push(new VersiondEntityRef(entityClassId, objId));

        while (!stack.isEmpty()) {
            VersiondEntityRef nextNode = stack.pop();
            changeVersionList = findChangeVersion(changeVersionList, nextNode.getId(), nextNode.getVersion()
                , nextNode.getEntityClassId(), stack);
        }

        return changeVersionList;
    }

    private List<ChangeVersion> findChangeVersion(List<ChangeVersion> changeVersionList, long objId, long version
        , long entityClassId, Stack<VersiondEntityRef> stack) {
        List<Changelog> relatedChangelog = replayService.getRelatedChangelog(objId, version, -1);
        if (!relatedChangelog.isEmpty()) {

            List<ChangeVersion> currentList = relatedChangelog.stream().map(x -> {
                ChangeVersion changeVersion = new ChangeVersion();
                changeVersion.setComment(x.getComment());
                changeVersion.setVersion(x.getVersion());
                return changeVersion;
            }).collect(Collectors.toList());

            changeVersionList =
                mergeSortedList(changeVersionList, currentList, Comparator.comparingLong(ChangeVersion::getVersion));

            /**
             * TODO current here losing fields
             */
            Optional<IEntityClass> entityClassOp = metaManager.load(entityClassId, "");
            if (entityClassOp.isPresent()) {
                IEntityClass entityClass = entityClassOp.get();
                EntityRelation mainRelation = replayService.replayRelation(entityClass, objId, relatedChangelog);
                //search next values
                mainRelation.getRelatedIds().forEach((key, value) -> {
                    long childEntity = key.getRightEntityClassId();
                    value.forEach(valueLife -> {
                        String idValue = valueLife.getValue();
                        if (idValue != null) {
                            try {
                                long idLong = Long.parseLong(idValue);
                                long endVersion = valueLife.getEnd();
                                stack.push(new VersiondEntityRef(childEntity, idLong, endVersion));
                            } catch (Exception ex) {
                                //TODO
                                logger.error("{}", ex);
                            }
                        } else {
                            logger.warn("REL on {} removed", key.getLeftEntityClassId());
                        }
                    });
                });
            }
        }

        return changeVersionList;
    }

    /**
     * TODO a new structure to represent
     */
    @Override
    public EntityAggDomain replayEntity(long entityClass, long objId, long version) {
        return replayService.replayAggDomain(entityClass, objId, version);
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
