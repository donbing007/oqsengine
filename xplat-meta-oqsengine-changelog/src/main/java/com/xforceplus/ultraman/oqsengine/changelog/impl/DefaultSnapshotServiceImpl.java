package com.xforceplus.ultraman.oqsengine.changelog.impl;

import com.xforceplus.ultraman.oqsengine.changelog.SnapshotService;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeSnapshot;
import com.xforceplus.ultraman.oqsengine.changelog.storage.write.SnapshotStorage;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * default snapshot
 */
public class DefaultSnapshotServiceImpl implements SnapshotService {

    @Resource
    private SnapshotStorage snapshotStorage;

    @Override
    public void saveSnapshot(ChangeSnapshot changeSnapshot) {
        snapshotStorage.saveSnapshot(changeSnapshot);
    }

    @Override
    public Optional<ChangeSnapshot> query(long objId, long version) {
        return snapshotStorage.query(objId, version);
    }
}
