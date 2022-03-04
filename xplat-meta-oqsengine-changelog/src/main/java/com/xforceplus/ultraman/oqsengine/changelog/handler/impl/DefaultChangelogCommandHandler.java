package com.xforceplus.ultraman.oqsengine.changelog.handler.impl;

import com.xforceplus.ultraman.oqsengine.changelog.ReplayService;
import com.xforceplus.ultraman.oqsengine.changelog.command.AddChangelog;
import com.xforceplus.ultraman.oqsengine.changelog.command.ChangelogCommand;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangedEvent;
import com.xforceplus.ultraman.oqsengine.changelog.entity.ChangelogStatefulEntity;
import com.xforceplus.ultraman.oqsengine.changelog.event.ChangelogEvent;
import com.xforceplus.ultraman.oqsengine.changelog.gateway.Gateway;
import com.xforceplus.ultraman.oqsengine.changelog.handler.ChangelogCommandHandler;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * default changelog command handler
 */
public class DefaultChangelogCommandHandler implements ChangelogCommandHandler<ChangelogCommand> {

    private Logger logger = LoggerFactory.getLogger(DefaultChangelogCommandHandler.class);

    @Resource
    private ReplayService replayService;

    @Resource
    private Gateway<ChangelogCommand, ChangelogEvent> gateway;

    @Override
    public boolean required(ChangelogCommand changelogCommand) {
        return changelogCommand != null;
    }

    @Override
    public void onCommand(ChangelogCommand command, Map<String, Object> context) {
        if (command instanceof AddChangelog) {
            logger.info("Got Add Changelog Command");
            ChangedEvent changedEvent = ((AddChangelog) command).getChangedEvent();
            if (changedEvent != null) {
                long entityClassId = changedEvent.getEntityClassId();
                long objId = ((AddChangelog) command).getObjId();
                Optional<ChangelogStatefulEntity> changelogStatefulEntity =
                    replayService.replayStatefulEntity(entityClassId, objId);
                if (changelogStatefulEntity.isPresent()) {
                    //this option will be empty in case the entityClass is not present
                    ChangelogStatefulEntity statefulEntity = changelogStatefulEntity.get();
                    List<ChangelogEvent> changelogEvents = statefulEntity.receive(command, context);
                    changelogEvents.forEach(gateway::dispatchEvent);
                }
            }
        } else {
            logger.error("Unknown command {}", command);
        }
    }
}
