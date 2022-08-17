package com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import java.util.List;

/**
 * 重算实例.
 *
 */
public class InitInstance {
    private IEntity entity;

    private IEntityClass entityClass;

    // 具有重算顺序的单元列表
    private List<InitInstanceUnit> initInstanceUnits;

    public InitInstance() {

    }

    /**
     * 默认构造器.
     *
     * @param entity 重算实例.
     * @param entityClass 对象
     * @param initInstanceUnits 字段单元
     */
    public InitInstance(IEntity entity, IEntityClass entityClass, List<InitInstanceUnit> initInstanceUnits) {
        this.entity = entity;
        this.entityClass = entityClass;
        this.initInstanceUnits = initInstanceUnits;
    }

    public IEntity getEntity() {
        return entity;
    }

    public void setEntity(IEntity entity) {
        this.entity = entity;
    }

    public IEntityClass getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(IEntityClass entityClass) {
        this.entityClass = entityClass;
    }

    public List<InitInstanceUnit> getInitInstanceUnits() {
        return initInstanceUnits;
    }

    public void setInitInstanceUnits(List<InitInstanceUnit> initInstanceUnits) {
        this.initInstanceUnits = initInstanceUnits;
    }


}
