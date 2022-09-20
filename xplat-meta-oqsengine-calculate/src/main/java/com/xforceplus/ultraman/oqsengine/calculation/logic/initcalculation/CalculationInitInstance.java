package com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation;

import com.xforceplus.ultraman.oqsengine.calculation.dto.ErrorCalculateInstance;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.List;
import java.util.Optional;

/**
 * 重算执行器，可以执行指定实例的指定字段进行重算及dryRun.
 *
 */
public interface CalculationInitInstance {
    /**
     * 重算当前实例所有计算字段.
     *
     * @param id 实例id
     * @param entityClass 对象
     * @param force 是否强制重算,false时只对emptyValue的计算字段重算
     * @return 重算后实例.
     */
    public Optional<IEntity> initInstance(Long id, IEntityClass entityClass, boolean force);

    /**
     * 重算当前实例集合中所有计算字段.
     *
     * @param ids 实例id集合
     * @param entityClass 对象
     * @param force 是否强制重算,false时只对emptyValue的计算字段重算
     * @param limit 单次重算上限
     * @return 重算后实例.
     */
    public List<IEntity> initInstances(List<Long> ids, IEntityClass entityClass, boolean force, Long limit);

    /**
     * 重算当前实例的指定字段.
     *
     * @param id 实例id
     * @param entityClass 对象
     * @param fields 重算对象字段
     * @param force 是否强制重算,false时只对emptyValue的计算字段重算
     * @return 重算后实例.
     */
    public Optional<IEntity> initField(Long id, IEntityClass entityClass, List<IEntityField> fields, boolean force);

    /**
     * 重算当前所有实例中的指定字段.
     *
     * @param ids 实例id集合
     * @param entityClass 对象
     * @param fields 重算对象字段
     * @param force 是否强制重算,false时只对emptyValue的计算字段重算
     * @param limit 单次重算上限
     * @return 重算后实例.
     */
    public List<IEntity> initFields(List<Long> ids, IEntityClass entityClass, List<IEntityField> fields, boolean force, Long limit);


    /**
     * check计算字段是否计算正确.
     *
     * @param ids   实例集合.
     * @param entityClass 对象.
     * @param fields 字段集合.
     * @param limit check上限.
     * @return 重算值和当前值不符的数据.
     */
    public List<ErrorCalculateInstance> initCheckFields(List<Long> ids, IEntityClass entityClass, List<IEntityField> fields, Long limit);

    /**
     * check计算字段是否计算正确.
     *
     * @param id 实例
     * @param entityClass 对象
     * @param fields 字段集合
     * @return 重算值和当前值不符的数据.
     */
    public Optional<ErrorCalculateInstance> initCheckField(Long id, IEntityClass entityClass, List<IEntityField> fields);


    /**
     * check计算字段是否计算正确.
     *
     * @param id 实例
     * @param entityClass 对象
     * @return 重算值和当前值不符的数据.
     */
    public  Optional<ErrorCalculateInstance>  initCheckInstance(Long id, IEntityClass entityClass);

    /**
     * check计算字段是否计算正确.
     *
     * @param ids 实例集合
     * @param entityClass 对象
     * @param limit 上限
     * @return 重算值和当前值不符的数据.
     */
    public List<ErrorCalculateInstance> initCheckInstances(List<Long> ids, IEntityClass entityClass, Long limit);

}
