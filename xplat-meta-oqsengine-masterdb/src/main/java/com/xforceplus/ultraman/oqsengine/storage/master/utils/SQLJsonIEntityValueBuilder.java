package com.xforceplus.ultraman.oqsengine.storage.master.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.utils.IEntityValueBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.AnyStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValueFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 通过json来构造出entityValue.
 *
 * @author dongbin
 * @version 0.1 2020/11/5 15:42
 * @since 1.8
 */
public class SQLJsonIEntityValueBuilder implements IEntityValueBuilder<String> {

    @Resource(name = "masterStorageStrategy")
    private StorageStrategyFactory storageStrategyFactory;

    @Override
    public IEntityValue build(long id, Map<String, IEntityField> fieldTable, String json) throws SQLException {
        JSONObject object = JSON.parseObject(json);

        String logicName;
        IEntityField field = null;
        FieldType fieldType;
        StorageStrategy storageStrategy;
        StorageValue newStorageValue;
        StorageValue oldStorageValue;
        // key 为物理储存名称,值为构造出的储存值.
        Map<String, EntityValuePack> storageValueCache = new HashMap<>(object.size());

        String sn;
        for (String storageName : object.keySet()) {
            sn = compatibleStorageName(storageName);
            try {

                // 为了找出物理名称中的逻辑字段名称.
                logicName = AnyStorageValue.getInstance(sn).logicName();
                field = fieldTable.get(logicName);

                if (field == null) {
                    continue;
                }

                fieldType = field.type();

                storageStrategy = this.storageStrategyFactory.getStrategy(fieldType);
                newStorageValue = StorageValueFactory.buildStorageValue(
                    storageStrategy.storageType(), sn, object.get(storageName));

                // 如果是多值.使用 stick 追加.
                if (storageStrategy.isMultipleStorageValue()) {
                    Optional<StorageValue> oldStorageValueOp = Optional.ofNullable(
                        storageValueCache.get(String.valueOf(field.id()))
                    ).map(x -> x.storageValue);

                    if (oldStorageValueOp.isPresent()) {
                        oldStorageValue = oldStorageValueOp.get();
                        storageValueCache.put(
                            String.valueOf(field.id()),
                            new EntityValuePack(field, oldStorageValue.stick(newStorageValue), storageStrategy));
                    } else {
                        storageValueCache.put(
                            String.valueOf(field.id()),
                            new EntityValuePack(field, newStorageValue, storageStrategy));
                    }
                } else {
                    // 单值
                    storageValueCache.put(String.valueOf(field.id()),
                        new EntityValuePack(field, newStorageValue, storageStrategy));
                }

            } catch (Exception ex) {
                throw new SQLException(ex.getMessage(), ex);
            }
        }

        IEntityValue values = new EntityValue(id);
        storageValueCache.values().stream().forEach(e -> {
            values.addValue(e.strategy.toLogicValue(e.logicField, e.storageValue));
        });


        return values;
    }

    // toEntity 临时解析结果.
    static class EntityValuePack {
        private IEntityField logicField;
        private StorageValue storageValue;
        private StorageStrategy strategy;

        public EntityValuePack(IEntityField logicField, StorageValue storageValue, StorageStrategy strategy) {
            this.logicField = logicField;
            this.storageValue = storageValue;
            this.strategy = strategy;
        }
    }

    // 兼容老版本数据.
    private String compatibleStorageName(String name) {
        if (name.startsWith(FieldDefine.ATTRIBUTE_PREFIX)) {
            //去除开头的F.
            return name.substring(1);
        } else {
            return name;
        }
    }
}
