package com.xforceplus.ultraman.oqsengine.changelog.event;

import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeVersion;
import java.util.Map;

/**
 * version event to return
 */
public class VersionEvent implements ChangelogEvent {

    private long objId;

    private ChangeVersion changeVersion;

    public VersionEvent(long objId, ChangeVersion changeVersion) {
        this.changeVersion = changeVersion;
        this.objId = objId;
    }

    public ChangeVersion getChangeVersion() {
        return changeVersion;
    }

    public long getObjId() {
        return objId;
    }

    @Override
    public Map<String, Object> getContext() {
        return null;
    }

    @Override
    public String toString() {
        return new StringBuilder()
            .append("VersionEvent{")
            .append("objId=").append(objId)
            .append(", changeVersion=")
            .append(changeVersion).append('}')
            .toString();
    }
}
