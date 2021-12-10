package com.xforceplus.ultraman.oqsengine.calculation.context;

import com.xforceplus.ultraman.oqsengine.calculation.dto.CalculationHint;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CalculationLogicFactory;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.idgenerator.client.BizIDGenerator;
import com.xforceplus.ultraman.oqsengine.lock.MultiResourceLocker;
import com.xforceplus.ultraman.oqsengine.lock.ResourceLocker;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.ConditionsSelectStorage;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.task.TaskCoordinator;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * 实例的计算字段的计算的上下文环境.
 *
 * @author dongbin
 * @version 0.1 2021/09/17 15:15
 * @since 1.8
 */
public interface CalculationContext {

    /**
     * 获得第一次被Focus的实体.
     *
     * @return 第一次被Focus的实体.
     */
    IEntity getSourceEntity();

    /**
     * 当前的目标实例.
     *
     * @return 目标实例.
     */
    IEntity getFocusEntity();

    /**
     * 获取焦点类型信息.
     *
     * @return 焦点类型信息.
     */
    IEntityClass getFocusClass();

    /**
     * 获取焦点字段.
     *
     * @return 焦点字段.
     */
    IEntityField getFocusField();

    /**
     * 当前是否维护.
     *
     * @return true是, false不是.
     */
    boolean isMaintenance();

    /**
     * 标识开始维护.
     */
    void startMaintenance();

    /**
     * 标识结束维护.
     */
    void stopMaintenance();

    /**
     * 设置当前的焦点实例,焦点实例类型.
     * 此焦点实例也会自动进入实例缓存池中.
     *
     * @param entity      焦点实例.
     * @param entityClass 焦点类型.
     */
    void focusEntity(IEntity entity, IEntityClass entityClass);

    /**
     * 设置当前的焦点的字段.
     *
     * @param field 焦点字段.
     */
    void focusField(IEntityField field);

    /**
     * 增加一个实例的值改变.
     *
     * @param valueChange 实例某个字段的值被更改的变化.
     */
    void addValueChange(ValueChange valueChange);

    /**
     * 获取指定实例的某个字段的变化.
     *
     * @param entity 目标实例.
     * @param field  目标字段.
     * @return 变化.
     */
    Optional<ValueChange> getValueChange(IEntity entity, IEntityField field);

    /**
     * 得到当前所有的字段改变.
     *
     * @return 改变列表.
     */
    Collection<ValueChange> getValueChanges();

    /**
     * 清理对象的字段值变化信息.
     *
     * @param entity 目标实例.
     * @param field  目标字段.
     */
    void removeValueChange(IEntity entity, IEntityField field);

    /**
     * 将指定实例加入实例缓存池.
     *
     * @param entity 目标对象.
     */
    void putEntityToCache(IEntity entity);

    /**
     * 从实例缓存池中查询对象.
     *
     * @param entityId 目标实例标识.
     * @return 结果.
     */
    Optional<IEntity> getEntityToCache(long entityId);

    /**
     * 从实例缓存池中删除对象.
     *
     * @param entityId 目标对象.
     */
    void removeEntityFromCache(long entityId);

    /**
     * 返回缓存中的所有实例.
     *
     * @return 实例列表.
     */
    Collection<IEntity> getEntitiesFormCache();

    /**
     * 计算触发场景.
     *
     * @return 场景标识.
     */
    CalculationScenarios getScenariso();

    /**
     * 当前事务.
     *
     * @return 事务.
     */
    Optional<Transaction> getCurrentTransaction();

    /**
     * 获取主库存的持久实例.
     *
     * @return 实例.
     */
    Optional<MasterStorage> getMasterStorage();

    /**
     * 获取当前的元信息管理器实例.
     *
     * @return 实例.
     */
    Optional<MetaManager> getMetaManager();

    /**
     * 获取 kv 储存实例.
     *
     * @return 实例.
     */
    Optional<KeyValueStorage> getKvStorage();

    /**
     * 获取事件总线.
     *
     * @return 事件总线.
     */
    Optional<EventBus> getEvnetBus();

    /**
     * 获取任务协调实例.
     *
     * @return 任务协调实例.
     */
    Optional<TaskCoordinator> getTaskCoordinator();

    /**
     * 字段计算逻辑工厂.
     *
     * @return 字段计算逻辑.
     */
    Optional<CalculationLogicFactory> getCalculationLogicFactory();

    /**
     * 条件查询器.
     *
     * @return 条件查询器.
     */
    Optional<ConditionsSelectStorage> getConditionsSelectStorage();

    /**
     * 普通自增编号的生成器.
     *
     * @return 实例.
     */
    Optional<BizIDGenerator> getBizIDGenerator();

    /**
     * 获取线程池.
     *
     * @return 任务线程池.
     */
    Optional<ExecutorService> getTaskExecutorService();

    /**
     * 获取资源锁.
     *
     * @return 资源锁.
     */
    Optional<ResourceLocker> getResourceLocker();

    /**
     * 获取资源锁连锁版本.
     *
     * @return 资源锁.
     */
    Optional<MultiResourceLocker> getMultiResourceLocker();

    /**
     * copy.
     */
    Object clone() throws CloneNotSupportedException;

    /**
     * 获取指定的资源.如果没有将抛出异常.
     *
     * @param supplier 资源返回器.
     * @param <T>      资源类型.
     * @return 资源.
     * @throws CalculationException 没有找到资源.
     */
    default <T> T getResourceWithEx(Supplier<Optional<T>> supplier) throws CalculationException {
        Optional<T> op = supplier.get();
        if (!op.isPresent()) {
            throw new CalculationException("Unable to get the specified resource.");
        } else {
            return op.get();
        }
    }

    /**
     * 创建提示.
     *
     * @param hint 提示信息.
     */
    void hint(IEntityField field, String hint);

    /**
     * 读取当前已经存在的提示.
     *
     * @return 提示列表.
     */
    Collection<CalculationHint> getHints();

    /**
     * 判断是否有hint.
     *
     * @return true 有, false 没有.
     */
    default boolean hasHint() {
        return !getHints().isEmpty();
    }
}
