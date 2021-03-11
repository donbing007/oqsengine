package com.xforceplus.ultraman.oqsengine.changelog;

import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeVersion;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangedEvent;
import com.xforceplus.ultraman.oqsengine.changelog.domain.Changelog;
import com.xforceplus.ultraman.oqsengine.changelog.domain.EntityAggDomain;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;

import java.util.List;

/**
 * change log service
 */
public interface ChangelogService {

    /**
     * get change log from an iEntity
     * @return
     */
    Changelog generateChangeLog(IEntityClass entityClass, ChangedEvent changedEvent);

    void saveChangeLogs(List<Changelog> changeLogs);

    /**
     * get changelog by id
     * @param objId
     * @return
     */
    List<ChangeVersion> getChangeLog(long objId, long entityClassId);

    EntityAggDomain replayEntity(long entityClass, long objId, long version);
}
