package com.xforceplus.ultraman.oqsengine.changelog;

import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeSnapshot;

import java.util.Optional;

/**
 * build snapshot and read snapshot
 */
public interface SnapshotService {

    void saveSnapshot(ChangeSnapshot changeSnapshot);

    Optional<ChangeSnapshot> query(long objId, long version);
}
