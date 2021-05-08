package com.xforceplus.ultraman.oqsengine.storage.value.strategy;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.common.BoolStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.common.DateTimeStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.common.EnumStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.common.LongStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.common.StringStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.common.StringsStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.common.UnsupportStorageStrategy;
import java.util.HashMap;
import java.util.Map;

/**
 * 储存逻辑转换策略工厂.
 *
 * @author dongbin
 * @version 0.1 2020/3/4 13:58
 * @since 1.8
 */
public class StorageStrategyFactory {

    private static final Map<FieldType, StorageStrategy> COMMON_STRATEGY = new HashMap();

    static {
        COMMON_STRATEGY.put(FieldType.LONG, new LongStorageStrategy());
        COMMON_STRATEGY.put(FieldType.STRING, new StringStorageStrategy());
        COMMON_STRATEGY.put(FieldType.BOOLEAN, new BoolStorageStrategy());
        COMMON_STRATEGY.put(FieldType.DATETIME, new DateTimeStorageStrategy());
        COMMON_STRATEGY.put(FieldType.ENUM, new EnumStorageStrategy());
        COMMON_STRATEGY.put(FieldType.STRINGS, new StringsStorageStrategy());
    }

    private static final StorageStrategy DEFAULT_STRATEGY = new UnsupportStorageStrategy();
    private Map<FieldType, StorageStrategy> strategies;

    /**
     * 得到一个默认的策略工厂,已经内置了基本类型的处理策略实现.
     *
     * @return 工厂实例.
     */
    public static StorageStrategyFactory getDefaultFactory() {
        return new StorageStrategyFactory(COMMON_STRATEGY);
    }

    private StorageStrategyFactory() {
    }

    private StorageStrategyFactory(Map<FieldType, StorageStrategy> strategies) {
        this.strategies = new HashMap(strategies);
    }

    /**
     * 注册一个新的字段类型策略,如果已经存在相应字段类型策略将被覆盖.
     *
     * @param type            目标字段类型.
     * @param storageStrategy 策略.
     */
    public void register(FieldType type, StorageStrategy storageStrategy) {
        if (strategies == null) {
            strategies = new HashMap<>();
        }

        strategies.put(type, storageStrategy);
    }

    /**
     * 根据逻辑值类型获取转换策略.
     *
     * @param type 逻辑类型.
     * @return 储存策略.
     */
    public StorageStrategy getStrategy(FieldType type) {
        StorageStrategy storageStrategy = strategies.get(type);
        if (storageStrategy == null) {
            return DEFAULT_STRATEGY;
        } else {
            return storageStrategy;
        }
    }

}
