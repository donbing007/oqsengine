package com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * 重算最小执行单元.
 *
 */
public class InitInstanceUnit {
    private IEntity entity;

    private IEntityClass entityClass;

    private IEntityField field;

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

    public IEntityField getField() {
        return field;
    }

    public void setField(IEntityField field) {
        this.field = field;
    }

    public InitInstanceUnit() {

    }

    /**
     * 默认构造器.
     *
     * @param entity 重算实例
     * @param entityClass 对象
     * @param field 字段
     */
    public InitInstanceUnit(IEntity entity, IEntityClass entityClass, IEntityField field) {
        this.entity = entity;
        this.entityClass = entityClass;
        this.field = field;
    }
}
