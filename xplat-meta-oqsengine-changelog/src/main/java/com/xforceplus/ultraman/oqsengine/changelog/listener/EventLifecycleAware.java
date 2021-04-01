package com.xforceplus.ultraman.oqsengine.changelog.listener;

import com.xforceplus.ultraman.oqsengine.event.ActualEvent;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.BuildPayload;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.DeletePayload;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.ReplacePayload;
import com.xforceplus.ultraman.oqsengine.event.payload.transaction.BeginPayload;
import com.xforceplus.ultraman.oqsengine.event.payload.transaction.CommitPayload;
import com.xforceplus.ultraman.oqsengine.event.payload.transaction.RollbackPayload;

/**
 * an interface for record lifecycle
 */
public interface EventLifecycleAware {

    void onTxCreate(ActualEvent<BeginPayload> begin);

    void onEntityCreate(ActualEvent<BuildPayload> create);

    void onEntityUpdate(ActualEvent<ReplacePayload> update);

    void onEntityDelete(ActualEvent<DeletePayload> delete);

    void onTxPreCommit(ActualEvent<CommitPayload> preCommit);

    void onTxCommitted(ActualEvent<CommitPayload> commited);

    void onTxPreRollBack(ActualEvent<RollbackPayload> preRollBack);

    void onTxRollBack(ActualEvent<RollbackPayload> preRollBack);

}
