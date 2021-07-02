package com.xforceplus.ultraman.oqsengine.calculation;

import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;

/**
 * 计算上下文.
 *
 * @author dongbin
 * @version 0.1 2021/07/01 17:44
 * @since 1.8
 */
public interface CalculationContext {

    /**
     * 计算触发是否为创建场景.
     *
     * @return true 是创建场景, false 不是.
     */
    public boolean isBuild();

    /**
     * 计算触发是否为更新场景.
     *
     * @return true 是更新场景, false 不是.
     */
    public boolean isReplace();

    /**
     * 获取计算依据信息.
     *
     * @return 依据字段信息.
     */
    public IValue getSourceValue();

    /**
     * 获取当前计算字段的类型元信息.
     *
     * @return 元信息实例.
     */
    public IEntityClass getSourceEntityClass();

    /**
     * 获取主库存的持久实例.
     *
     * @return 实例.
     */
    public MasterStorage getMasterStorage();

    /**
     * 获取当前的元信息管理器实例.
     *
     * @return 实例.
     */
    public MetaManager getMetaManager();

}
