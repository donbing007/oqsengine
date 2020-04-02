package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.SphinxQLIndexStorageCommandInvoker;
import com.xforceplus.ultraman.oqsengine.storage.master.command.SQLMasterStorageCommandInvoker;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.undo.UndoFactory;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommandInvoker;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.store.RedisUndoLogStore;
import com.xforceplus.ultraman.oqsengine.storage.undo.store.UndoLogStore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/24/2020 3:33 PM
 * 功能描述:
 * 修改历史:
 */
@Configuration
public class UndoConfiguration {

    @Bean
    public UndoLogStore undoLogStore(RedissonClient redissonClient){
        return new RedisUndoLogStore(redissonClient);
    }

    @Bean
    public StorageCommandInvoker sphinxQLIndexStorageCommandInvoker(@Value("${storage.index.name:oqsindex}") String indexTableName) {
        return new SphinxQLIndexStorageCommandInvoker(indexTableName);
    }

    @Bean
    public StorageCommandInvoker sqlMasterStorageCommandInvoker() {
        return new SQLMasterStorageCommandInvoker();
    }

    @Bean
    public UndoFactory undoFactory(UndoLogStore undoLogStore,
                                   StorageCommandInvoker sphinxQLIndexStorageCommandInvoker,
                                   StorageCommandInvoker sqlMasterStorageCommandInvoker,
                                   Selector<DataSource> indexWriteDataSourceSelector,
                                   Selector<DataSource> masterDataSourceSelector){
        UndoFactory undoFactory = new UndoFactory(undoLogStore);
        undoFactory.register(DbTypeEnum.INDEX, sphinxQLIndexStorageCommandInvoker);
        undoFactory.register(DbTypeEnum.MASTOR, sqlMasterStorageCommandInvoker);
        undoFactory.register(DbTypeEnum.INDEX, indexWriteDataSourceSelector);
        undoFactory.register(DbTypeEnum.MASTOR, masterDataSourceSelector);

        return undoFactory;
    }
}
