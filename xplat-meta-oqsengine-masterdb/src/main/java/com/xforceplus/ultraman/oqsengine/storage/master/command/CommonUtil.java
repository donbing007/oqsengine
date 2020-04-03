package com.xforceplus.ultraman.oqsengine.storage.master.command;

import com.alibaba.fastjson.JSONObject;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

import java.sql.SQLException;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 4/3/2020 6:46 PM
 * 功能描述:
 * 修改历史:
 */
public class CommonUtil {
    public static void checkId(IEntity entity) throws SQLException {
        if (entity.id() == 0) {
            throw new SQLException("Invalid entity`s id.");
        }
    }

    // 属性名称使用的是属性 id.
    public static String toJson(StorageStrategyFactory storageStrategyFactory, IEntityValue value) {

        JSONObject object = new JSONObject();
        StorageStrategy storageStrategy;
        StorageValue storageValue;
        for (IValue logicValue : value.values()) {
            storageStrategy = storageStrategyFactory.getStrategy(logicValue.getField().type());
            storageValue = storageStrategy.toStorageValue(logicValue);
            while (storageValue != null) {
                object.put(storageValue.storageName(), storageValue.value());
                storageValue = storageValue.next();
            }
        }
        return object.toJSONString();
    }

}
