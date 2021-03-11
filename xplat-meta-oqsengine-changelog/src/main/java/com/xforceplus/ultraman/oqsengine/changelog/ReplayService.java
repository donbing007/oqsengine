package com.xforceplus.ultraman.oqsengine.changelog;

import com.xforceplus.ultraman.oqsengine.changelog.domain.Changelog;
import com.xforceplus.ultraman.oqsengine.changelog.domain.EntityAggDomain;
import com.xforceplus.ultraman.oqsengine.changelog.domain.EntityDomain;
import com.xforceplus.ultraman.oqsengine.changelog.domain.EntityRelation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;

import java.util.List;

/**
 * rebuild service
 */
public interface ReplayService {

    List<Changelog> getRelatedChangelog(long id);

    List<Changelog> getRelatedChangelog(long id, long version);

    EntityDomain replaySimpleDomain(long entityClass, long id, long version);

    EntityRelation replayRelation(IEntityClass entityClass, long id, List<Changelog> changelogList);

    EntityAggDomain replayAggDomain(long entityClass, long id, long version);

}
