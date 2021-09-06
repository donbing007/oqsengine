package com.xforceplus.ultraman.oqsengine.changelog.relation;

import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangedEvent;
import com.xforceplus.ultraman.oqsengine.changelog.domain.Changelog;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;

import java.util.List;

/**
 * relation aware changeLog
 */
public interface RelationAwareChangelog {

    boolean require(Relationship relation);

    /**
     * @param relation
     * @param entityClass
     * @param changedEvent
     * @return
     */
    List<Changelog> generateOuterChangelog(
        Relationship relation, IEntityClass entityClass
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
