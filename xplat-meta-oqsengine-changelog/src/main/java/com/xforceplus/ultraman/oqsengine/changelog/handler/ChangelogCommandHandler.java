package com.xforceplus.ultraman.oqsengine.changelog.handler;

import com.xforceplus.ultraman.oqsengine.changelog.command.ChangelogCommand;

import java.util.Map;

public interface ChangelogCommandHandler<T extends ChangelogCommand> {

    boolean required(ChangelogCommand changelogEvent);

    void onCommand(T command, Map<String, Object> context);
}
