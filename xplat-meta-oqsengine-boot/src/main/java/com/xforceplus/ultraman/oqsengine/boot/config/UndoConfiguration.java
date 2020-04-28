package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.BuildStorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.DeleteStorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.ReplaceStorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.undo.UndoExecutor;
import com.xforceplus.ultraman.oqsengine.storage.undo.UndoFactory;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.DefaultStorageCommandExecutor;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommandExecutor;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbType;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpType;
import com.xforceplus.ultraman.oqsengine.storage.undo.store.RedisUndoLogStore;
import com.xforceplus.ultraman.oqsengine.storage.undo.store.SimpleUndoLogStore;
import com.xforceplus.ultraman.oqsengine.storage.undo.store.UndoLogStore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
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

    @ConditionalOnExpression("${store.simple.enabled} == true")
    @Bean("undoLogStore")
    public UndoLogStore simpleUndoLogStore(){
        return new SimpleUndoLogStore();
    }

    @ConditionalOnExpression("${store.redis.enabled} == true")
    @Bean("undoLogStore")
    public UndoLogStore redisUndoLogStore(RedissonClient redissonClient){
        return new RedisUndoLogStore(redissonClient);
    }

    @Bean
    public StorageCommandExecutor storageCommandInvoker(
            @Value("${storage.index.name:oqsindex}") String indexTableName,
            Selector<String> tableNameSelector
    ) {
        DefaultStorageCommandExecutor storageCommandInvoker = new DefaultStorageCommandExecutor();
        storageCommandInvoker.register(DbType.INDEX, OpType.BUILD, new BuildStorageCommand(indexTableName));
        storageCommandInvoker.register(DbType.INDEX, OpType.REPLACE, new ReplaceStorageCommand(indexTableName));
        storageCommandInvoker.register(DbType.INDEX, OpType.DELETE, new DeleteStorageCommand(indexTableName));

        storageCommandInvoker.register(DbType.MASTER, OpType.BUILD, new com.xforceplus.ultraman.oqsengine.storage.master.command.BuildStorageCommand(tableNameSelector));
        storageCommandInvoker.register(DbType.MASTER, OpType.REPLACE, new com.xforceplus.ultraman.oqsengine.storage.master.command.ReplaceStorageCommand(tableNameSelector));
        storageCommandInvoker.register(DbType.MASTER, OpType.DELETE, new com.xforceplus.ultraman.oqsengine.storage.master.command.DeleteStorageCommand(tableNameSelector));

        return storageCommandInvoker;
    }

    @Bean
    public UndoExecutor undoExecutor(UndoLogStore undoLogStore, StorageCommandExecutor storageCommandInvoker){
        return new UndoExecutor(undoLogStore, storageCommandInvoker);
    }

    @Bean
    public UndoFactory undoFactory(){
        return new UndoFactory();
    }
}
