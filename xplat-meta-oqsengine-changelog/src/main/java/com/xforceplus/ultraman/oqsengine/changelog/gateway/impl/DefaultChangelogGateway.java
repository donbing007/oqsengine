package com.xforceplus.ultraman.oqsengine.changelog.gateway.impl;

import com.xforceplus.ultraman.oqsengine.changelog.command.ChangelogCommand;
import com.xforceplus.ultraman.oqsengine.changelog.event.ChangelogEvent;
import com.xforceplus.ultraman.oqsengine.changelog.gateway.Gateway;
import com.xforceplus.ultraman.oqsengine.changelog.handler.ChangelogCommandHandler;
import com.xforceplus.ultraman.oqsengine.changelog.handler.ChangelogEventHandler;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;

/**
 * default command and event gateway
 */
public class DefaultChangelogGateway implements Gateway<ChangelogCommand, ChangelogEvent> {

    @Resource
    private List<ChangelogCommandHandler> changelogCommandHandlerList;

    @Resource
    private List<ChangelogEventHandler> changelogEventHandlerList;

    public DefaultChangelogGateway() {}

    public List<ChangelogCommandHandler> getChangelogCommandHandlerList() {
        return changelogCommandHandlerList;
    }

    public void setChangelogCommandHandlerList(List<ChangelogCommandHandler> changelogCommandHandlerList) {
        this.changelogCommandHandlerList = changelogCommandHandlerList;
    }

    public List<ChangelogEventHandler> getChangelogEventHandlerList() {
        return changelogEventHandlerList;
    }

    public void setChangelogEventHandlerList(List<ChangelogEventHandler> changelogEventHandlerList) {
        this.changelogEventHandlerList = changelogEventHandlerList;
    }

    @Override
    public void fireAndForget(ChangelogCommand changelogCommand, Map<String, Object> context) {
        changelogCommandHandlerList.stream()
            .filter(x -> x.required(changelogCommand))
            .forEach(x -> x.onCommand(changelogCommand, context));
    }

    @Override
    public void dispatchEvent(ChangelogEvent changelogEvent) {
        changelogEventHandlerList.stream()
            .filter(x -> x.required(changelogEvent))
            .forEach(x -> x.onEvent(changelogEvent));
    }
}
