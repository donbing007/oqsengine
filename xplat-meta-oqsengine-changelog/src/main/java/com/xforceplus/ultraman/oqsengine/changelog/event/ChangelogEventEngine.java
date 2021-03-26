package com.xforceplus.ultraman.oqsengine.changelog.event;

//import com.xforceplus.ultraman.oqsengine.changelog.domain.EntityDomain;
//
//import javax.annotation.Resource;
//
///**
// * a engine to handle changelog
// */
//public class ChangelogEventEngine implements EventEngine{
//
//    @Resource
//    private ReplayService replayService;
//
//    @Resource
//    private ChangelogService changelogService;
//
//    /**
//     * @param changedEvent
//     */
//    @Override
//    synchronized public void feed(ChangedEvent changedEvent) {
//        if(changedEvent != null){
//            long entityClassId = changedEvent.getEntityClassId();
//            long objId = changedEvent.getId();
//            long version = changedEvent.getCommitId();
//            EntityDomain entityDomain = replayService.replaySimpleDomain(entityClassId, objId, version);
//            changelogService.generateChangeLog(changedEvent);
//        }
//    }
//}
