package com.xforceplus.ultraman.oqsengine.changelog.relation;

import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangedEvent;
import com.xforceplus.ultraman.oqsengine.changelog.domain.Changelog;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.oqs.OqsRelation;

import java.util.List;

/**
 * relation aware changeLog
 */
public interface RelationAwareChangelog {

    boolean require(OqsRelation relation);

    /**
     * @param relation
     * @param entityClass
     * @param changedEvent
     * @return
     */
    List<Changelog> generateOuterChangelog(
            OqsRelation relation, IEntityClass entityClass
            , ChangedEvent changedEvent);

    /**
     * TODO
     * changes only record version but not changes current
     * @return
     */
    default Changelog noChangesChangelog(long id, ChangedEvent changedEvent){
        Changelog changelog = new Changelog();
        return changelog;
    }
}
