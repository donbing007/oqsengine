package com.xforceplus.ultraman.oqsengine.changelog.relation;

import com.xforceplus.ultraman.oqsengine.changelog.ReplayService;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeValue;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangedEvent;
import com.xforceplus.ultraman.oqsengine.changelog.domain.Changelog;
import com.xforceplus.ultraman.oqsengine.changelog.domain.EntityRelation;
import com.xforceplus.ultraman.oqsengine.common.id.IdGenerator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldLikeRelationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsRelation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * TODO
 * TODO related-value?
 * many to one
 */
public class ManyToOneRelationChangelog implements RelationAwareChangelog {

    private Logger logger = LoggerFactory.getLogger(ManyToOneRelationChangelog.class);

    @Resource(name = "snowflakeIdGenerator")
    private IdGenerator<Long> idGenerator;

    @Resource
    private ReplayService replayService;

    //TODO
    @Override
    public boolean require(OqsRelation relation) {
        return FieldLikeRelationType.MANY2ONE.getName().equalsIgnoreCase(relation.getRelationType());
    }

    /**
     * TODO
     *
     * @param commitId
     * @param timestamp
     * @param comment
     * @return
     */
    private Changelog changelogForOuter(long entityClassId, Long objId, long fieldId, long relatedObjId, long commitId, long timestamp, String comment, ChangeValue.Op op) {

        Changelog changelog = new Changelog();
        changelog.setcId(idGenerator.next());
        changelog.setEntityClass(entityClassId);
        changelog.setCreateTime(timestamp);
        changelog.setId(relatedObjId);
        changelog.setVersion(commitId);
        changelog.setComment(comment);
        ChangeValue changeValue = new ChangeValue();
        changeValue.setReferenceSet(true);
        changeValue.setFieldId(fieldId);
        changeValue.setOp(op);
        changeValue.setRawValue(objId.toString());
        changelog.setChangeValues(Collections.singletonList(changeValue));
        return changelog;
    }

    private boolean noChanges(OqsRelation relation, ChangedEvent changedEvent) {
        long changedEntityClass = changedEvent.getEntityClassId();
        return relation.getEntityClassId() != changedEntityClass;
    }

    /**
     * @param relation    a relation entityClass owns should always a reverse relation
     * @param entityClass
     * @return
     */
    @Override
    public List<Changelog> generateOuterChangelog(OqsRelation relation, IEntityClass entityClass, ChangedEvent changedEvent) {

        //TODO
        //return no changes value
        if (noChanges(relation, changedEvent)) {
            return Collections.singletonList(noChangesChangelog(idGenerator.next(), changedEvent));
        }

        //real changes
        IEntity beforeChange = null;
        IEntity afterChange = null;
        long commitId = changedEvent.getCommitId();
        long timestamp = changedEvent.getTimestamp();
        String comment = changedEvent.getComment();
        long id = changedEvent.getId();

        /**
         * get related field
         */
        long fieldId = relation.getEntityField().id();

        /**
         * a companionId is a field id which exists on
         */
        long companionFieldId = fieldId;
        if (relation.isCompanion()) {
            companionFieldId = relation.getCompanionRelation();
        }

        Optional<IValue> beforeOuter = Optional.ofNullable(beforeChange).flatMap(change -> change.entityValue()
                .values().stream().filter(x -> x.getField().id() == fieldId).findFirst());
        Optional<IValue> afterOuter = Optional.ofNullable(afterChange).flatMap(change -> change.entityValue()
                .values().stream().filter(x -> x.getField().id() == fieldId).findFirst());

        /**
         * three case
         *  0. none => no before, no after
         *  1. new => no before, after
         *  2. change => before, after
         *  3. remove => before, no after
         */
        boolean before = beforeOuter.isPresent();
        boolean after = afterOuter.isPresent();

        if (!before && after) {
            //only change occurs
            //remove old and add new
            long relatedObjIdAfter = afterOuter.get().valueToLong();
            Optional<Changelog> changelog = genChangelogForRelatedEntity(relatedObjIdAfter, entityClass, id
                    , companionFieldId, ChangeValue.Op.ADD, commitId, timestamp, comment);
            if (changelog.isPresent()) {
                return Collections.singletonList(changelog.get());
            }
        } else if (before && after) {
            //change
            //find old
            //generate old
            //find new
            //generate new
            if (!afterOuter.get().getValue().equals(beforeOuter.get().getValue())) {

                List<Changelog> changelogs = new LinkedList<>();

                //only change occurs
                //remove old and add new
                long relatedObjIdAfter = afterOuter.get().valueToLong();
                genChangelogForRelatedEntity(relatedObjIdAfter, entityClass, id, companionFieldId, ChangeValue.Op.ADD
                        , commitId, timestamp, comment)
                        .ifPresent(changelogs::add);

                long relatedObjIdBefore = beforeOuter.get().valueToLong();
                genChangelogForRelatedEntity(relatedObjIdBefore, entityClass, id, companionFieldId, ChangeValue.Op.DEL
                        , commitId, timestamp, comment)
                        .ifPresent(changelogs::add);

                return changelogs;
            }
        } else if (before && !after) {
            long relatedObjIdBefore = beforeOuter.get().valueToLong();
            Optional<Changelog> changelog = genChangelogForRelatedEntity(relatedObjIdBefore
                    , entityClass, id, companionFieldId, ChangeValue.Op.DEL, commitId, timestamp, comment);

            if (changelog.isPresent()) {
                return Collections.singletonList(changelog.get());
            }
        }

        return Collections.emptyList();
    }

    /**
     * gen changelog for related Entity
     *
     * @return
     */
    private Optional<Changelog> genChangelogForRelatedEntity(long relatedObjId
            , IEntityClass relatedEntityClass, long selfId, long fieldId, ChangeValue.Op op
            , long commitId, long timestamp, String comment
    ) {

        Optional<EntityRelation> entityRelation = findEntityByOuterKey(relatedEntityClass, relatedObjId);
        if (entityRelation.isPresent()) {
            Changelog changelog = changelogForOuter(relatedEntityClass.id(), selfId, fieldId, relatedObjId
                    , commitId, timestamp, comment, op);
            return Optional.ofNullable(changelog);
        }
        return Optional.empty();
    }

    /**
     * find entityClass by some key
     *
     * @param
     * @return
     */
    Optional<EntityRelation> findEntityByOuterKey(IEntityClass entityClass, long id) {

        List<Changelog> relatedChangelogList = replayService.getRelatedChangelog(id);

        if (relatedChangelogList.isEmpty()) {
            return Optional.empty();
        }

        EntityRelation entityRelation = replayService.replayRelation(entityClass, id, relatedChangelogList);
        return Optional.ofNullable(entityRelation);
    }
}
