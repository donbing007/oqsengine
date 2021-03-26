package com.xforceplus.ultraman.oqsengine.changelog.handler.impl;

import com.xforceplus.ultraman.oqsengine.changelog.event.ChangelogEvent;
import com.xforceplus.ultraman.oqsengine.changelog.event.VersionEvent;
import com.xforceplus.ultraman.oqsengine.changelog.handler.ChangelogEventHandler;
import com.xforceplus.ultraman.oqsengine.changelog.storage.query.QueryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.Collections;

/**
 * version event handler
 */
public class VersionEventHandler implements ChangelogEventHandler<VersionEvent> {

    @Resource
    private QueryStorage queryStorage;

    private Logger logger = LoggerFactory.getLogger(VersionEventHandler.class);

    @Override
    public boolean required(ChangelogEvent changelogEvent) {
        return changelogEvent instanceof VersionEvent;
    }

    @Override
    public void onEvent(VersionEvent changelogEvent) {
        try {
            queryStorage.saveChangeVersion(changelogEvent.getObjId()
                    , Collections.singletonList(changelogEvent.getChangeVersion()));
        } catch (SQLException e) {
            logger.error("{}", e);
        }
    }
}
