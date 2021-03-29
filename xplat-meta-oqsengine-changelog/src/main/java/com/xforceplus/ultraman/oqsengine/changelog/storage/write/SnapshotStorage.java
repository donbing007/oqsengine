package com.xforceplus.ultraman.oqsengine.changelog.storage.write;

import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeSnapshot;
import io.vavr.control.Either;

import java.sql.SQLException;
import java.util.Optional;

public interface SnapshotStorage {

    Either<SQLException, Integer> saveSnapshot(ChangeSnapshot changeSnapshot);

    Optional<ChangeSnapshot> query(long objId, long version);

}
