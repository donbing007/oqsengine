package com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor;

import com.xforceplus.ultraman.oqsengine.common.map.MapUtils;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.ValueWithEmpty;
import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.pojo.BaseMasterStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.value.AnyStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * 主库存任务执行器抽像.
 *
 * @author dongbin
 * @version 0.1 2022/3/10 15:47
 * @since 1.8
 */
public abstract class AbstractMasterTaskExecutor<R, T> extends AbstractJdbcTaskExecutor<R, T> {

    public AbstractMasterTaskExecutor(String tableName,
                                      TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public AbstractMasterTaskExecutor(String tableName,
                                      TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    /**
     * 将字段物理值映射转换成原始字段和储存值.<br>
     * 结果中key= AnyStorageValue.ATTRIBUTE_PREFIX 开头的字段标识, Value为实际的写入储存值.<br>
     * 注意: 如果StorageValue.isEmpty() 为true,那么在创建的时候表示忽略,更新时候表示删除.<br>
     * 但是如果这个storageValue实例含有附件,那么创建的时候表示只保存附件,更新不应该设置附件.<br>
     *
     * @param storageValues 字段物理值映射.
     * @return 字段储存值.
     */
    protected Map<String, Object> toPainValues(Map<IEntityField, StorageValue> storageValues) {

        //KEY=物理储存字段, VALUE为物理储存值.
        Map<String, Object> values = new HashMap<>(MapUtils.calculateInitSize(storageValues.size()));

        StorageValue storageValue = null;

        for (IEntityField field : storageValues.keySet()) {
            storageValue = storageValues.get(field);

            if (storageValue.isEmpty()) {

                /*
                如果为空,含有附件表示只需要删除目标字段,但是保留其附件.
                否则,字段和附件都将被表示为空表示删除.
                 */
                values.put(String.format("%s%s", AnyStorageValue.ATTRIBUTE_PREFIX, storageValue.storageName()),
                    ValueWithEmpty.EMPTY_VALUE);
                if (storageValue.haveAttachment()) {
                    StorageValue attachmentStorageValue = storageValue.getAttachment();
                    values.put(
                        String.format("%s%s",
                            AnyStorageValue.ATTACHMENT_PREFIX,
                            attachmentStorageValue.storageName()), attachmentStorageValue.value());
                } else {
                    values.put(String.format("%s%s", AnyStorageValue.ATTACHMENT_PREFIX, storageValue.storageName()),
                        ValueWithEmpty.EMPTY_VALUE);
                }

            } else {

                while (true) {
                    values.put(
                        String.format("%s%s", AnyStorageValue.ATTRIBUTE_PREFIX, storageValue.storageName()),
                        storageValue.value());

                    if (storageValue.next() != null) {
                        storageValue = storageValue.next();
                    } else {
                        break;
                    }
                }

                // 处理附件.
                if (storageValue.haveAttachment()) {
                    StorageValue attachmentStorageValue = storageValue.getAttachment();
                    values.put(
                        String.format("%s%s",
                            AnyStorageValue.ATTACHMENT_PREFIX,
                            attachmentStorageValue.storageName()), attachmentStorageValue.value());
                }
            }

        }
        return values;
    }

    /**
     * 设置动态对象操作状态.
     *
     * @param storageEntities 实体表示.
     * @param results         操作结果.
     */
    protected void setDynamicProcessStatus(BaseMasterStorageEntity[] storageEntities, boolean[] results) {
        for (int i = 0; i < storageEntities.length; i++) {
            storageEntities[i].setDynamicSuccess(results[i]);
        }
    }
}
