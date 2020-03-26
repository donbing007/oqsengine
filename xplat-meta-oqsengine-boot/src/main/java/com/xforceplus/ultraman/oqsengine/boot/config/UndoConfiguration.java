package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.storage.executor.AutoCreateTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoShardTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.SphinxQLIndexAction;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.SphinxQLIndexUndoExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.SQLMasterAction;
import com.xforceplus.ultraman.oqsengine.storage.master.SQLMasterUndoExecutor;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.sql.ConnectionTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.sql.SphinxQLTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.UndoExecutor;
import com.xforceplus.ultraman.oqsengine.storage.undo.store.RedisUndoLogStore;
import com.xforceplus.ultraman.oqsengine.storage.undo.store.UndoLogStore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/24/2020 3:33 PM
 * 功能描述:
 * 修改历史:
 */
@Configuration
public class UndoConfiguration {

    @Autowired
    private TransactionManager tm;

    @Value("${storage.master.query.worker:0}")
    private int masterWorkerSize;

    @Value("${storage.master.query.timeout:3000}")
    private long masterQueryTimeout;

    @Value("${storage.index.name:oqsindex}")
    private String indexTableName;


    @Bean
    public UndoLogStore undoLogStore(RedissonClient redissonClient){
        return new RedisUndoLogStore(redissonClient);
    }

    @Bean
    public SphinxQLIndexAction sphinxQLIndexAction() {
        SphinxQLIndexAction sphinxQLIndexAction = new SphinxQLIndexAction();
        sphinxQLIndexAction.setIndexTableName(indexTableName);
        return sphinxQLIndexAction;
    }

    @Bean
    public SQLMasterAction sqlMasterAction() {
        return new SQLMasterAction();
    }

    @Bean
    public SphinxQLIndexUndoExecutor sphinxQLIndexUndoExecutor(SphinxQLIndexAction sphinxQLIndexAction, UndoLogStore undoLogStore){
        return new SphinxQLIndexUndoExecutor(sphinxQLIndexAction, undoLogStore);
    }

    @Bean
    public SQLMasterUndoExecutor sqlMasterUndoExecutor(SQLMasterAction sqlMasterAction, UndoLogStore undoLogStore){
        return new SQLMasterUndoExecutor(sqlMasterAction, undoLogStore);
    }

    @Bean
    public TransactionExecutor storageSphinxQLTransactionExecutor(SphinxQLIndexUndoExecutor sphinxQLIndexUndoExecutor) {
        return new AutoShardTransactionExecutor(tm, SphinxQLTransactionResource.class, sphinxQLIndexUndoExecutor);
    }

    @Bean
    public TransactionExecutor storageJDBCTransactionExecutor(SQLMasterUndoExecutor sqlMasterUndoExecutor) {
        return new AutoShardTransactionExecutor(tm, ConnectionTransactionResource.class, sqlMasterUndoExecutor);
    }

    @Bean
    public TransactionExecutor serviceTransactionExecutor(SQLMasterUndoExecutor sqlMasterUndoExecutor) {
        return new AutoCreateTransactionExecutor(tm, sqlMasterUndoExecutor);
    }
}
