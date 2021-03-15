package com.xforceplus.ultraman.oqsengine.changelog.handler.impl;

import com.xforceplus.ultraman.oqsengine.changelog.command.AddChangelog;
import com.xforceplus.ultraman.oqsengine.changelog.command.ChangelogCommand;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangedEvent;
import com.xforceplus.ultraman.oqsengine.changelog.event.ChangelogEvent;
import com.xforceplus.ultraman.oqsengine.changelog.event.PropagationChangelogEvent;
import com.xforceplus.ultraman.oqsengine.changelog.gateway.Gateway;
import com.xforceplus.ultraman.oqsengine.changelog.handler.ChangelogEventHandler;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * propagation the event
 */
public class PropagationEventHandler implements ChangelogEventHandler<PropagationChangelogEvent> {

    /**
     * is the context ids  --> Set<Long>
     */
    private static final String FOOTPRINT = "footprint";

    @Resource
    private Gateway<ChangelogCommand, ChangelogEvent> gateway;

    @Override
    public boolean required(ChangelogEvent changelogEvent) {
        return changelogEvent instanceof PropagationEventHandler;
    }

    @Override
    public void onEvent(PropagationChangelogEvent changelogEvent) {
        ChangedEvent changedEvent = changelogEvent.getChangedEvent();
        long destinationObjId = changelogEvent.getDestinationObjId();
        long entityClassId = changelogEvent.getEntityClassId();
        Map<String, Object> context = changelogEvent.getContext();
        if(shouldDeliver(changelogEvent)) {
            gateway.fireAndForget(new AddChangelog(destinationObjId, entityClassId, changedEvent), context);
        }
    }

    private boolean shouldDeliver(PropagationChangelogEvent changelogEvent){
        Map<String, Object> context = changelogEvent.getContext();
        Object footprints = context.get(FOOTPRINT);
        if(footprints == null){
            footprints = new HashSet<>();
            context.put(FOOTPRINT, footprints);
        }

        Set<Long> footprintSet = (Set<Long>) footprints;
        if(footprintSet.contains(changelogEvent.getDestinationObjId())){
            return false;
        } else{
            footprintSet.add(changelogEvent.getDestinationObjId());
            return true;
        }
    }
}
