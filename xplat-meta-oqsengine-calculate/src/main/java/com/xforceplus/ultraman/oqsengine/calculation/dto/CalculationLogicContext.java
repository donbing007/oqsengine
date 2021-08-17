package com.xforceplus.ultraman.oqsengine.calculation.dto;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.idgenerator.client.BizIDGenerator;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.task.TaskCoordinator;
import java.util.Collection;
import java.util.Optional;

/**
 * 计算上下文.
 *
 * @author dongbin
 * @version 0.1 2021/07/01 17:44
 * @since 1.8
 */
public interface CalculationLogicContext {

    /**
     * 计算触发是否为创建场景.
     *
     * @return true 是创建场景, false 不是.
     */
    boolean isBuild();

    /**
     * 计算触发是否为更新场景.
     *
     * @return true 是更新场景, false 不是.
     */
    boolean isReplace();

    /**
     * 当前处理的entity实例.
     *
     * @return 对象实例.
     */
    IEntity getEntity();

    /**
     * 设置当前的焦点字段.
     *
     * @param field 焦点字段.
     */
    void focusField(IEntityField field);

    /**
     * 当前的焦点字段.
     *
     * @return 字段信息.
     */
    IEntityField getFocusField();

    /**
     * 获取当前计算字段的类型元信息.
     *
     * @return 元信息实例.
     */
    IEntityClass getEntityClass();

    /**
     * 获取主库存的持久实例.
     *
     * @return 实例.
     */
    MasterStorage getMasterStorage();

    /**
     * 获取当前的元信息管理器实例.
     *
     * @return 实例.
     */
    MetaManager getMetaManager();

    /**
     * 获取 kv 储存实例.
     *
     * @return 实例.
     */
    KeyValueStorage getKvStorage();

    /**
     * 获取任务协调实例.
     *
     * @return 任务协调实例.
     */
    TaskCoordinator getTaskCoordinator();

    /**
     * 获取属性.
     *
     * @param key 属性key.
     * @return 属性值.
     */
    Optional<Object> getAttribute(String key);

    /**
     * 创建提示.
     *
     * @param hint 提示信息.
     */
    void hint(String hint);

    /**
     * 读取当前已经存在的提示.
     *
     * @return 提示列表.
     */
    Collection<CalculationHint> getHints();

    /**
     * 获取连续且偏序的ID生成器.
     *
     * @return 实例.
     */
    LongIdGenerator getLongContinuousPartialOrderIdGenerator();

    /**
     * 获取不连接续,但偏序的ID生成器.
     *
     * @return 实例.
     */
    LongIdGenerator getLongNoContinuousPartialOrderIdGenerator();

    /**
     * 普通自增编号的生成器.
     *
     * @return 实例.
     */
    BizIDGenerator getBizIDGenerator();
}
