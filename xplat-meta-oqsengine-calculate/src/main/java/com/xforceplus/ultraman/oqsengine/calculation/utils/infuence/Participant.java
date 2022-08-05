package com.xforceplus.ultraman.oqsengine.calculation.utils.infuence;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.Collection;
import java.util.Optional;

/**
 * 影响树参与者.
 */
public interface Participant {

    /**
     * 当前参与者的标识.
     *
     * @return 标识.
     */
    public String getId();

    /**
     * 当前参与者的元类型.
     *
     * @return 元类型.
     */
    public IEntityClass getEntityClass();

    /**
     * 当前参与者的字段.
     *
     * @return 字段.
     */
    public IEntityField getField();

    /**
     * 当前参与者影响的实例.
     *
     * @return 影响实例列表.
     */
    public Collection<IEntity> getAffectedEntities();

    /**
     * 增加一个被影响的实例.
     *
     * @param entity 初当前参与者影响的实例.
     */
    public void addAffectedEntity(IEntity entity);

    /**
     * 移除一个被影响的实例.
     *
     * @param id 实例标识.
     * @return 被影响的实例.
     */
    public Optional<IEntity> removeAffectedEntities(long id);

    /**
     * 当前参与者附件.
     *
     * @return 附件.
     */
    public Optional<Object> getAttachment();

    /**
     * 是否影响源.
     *
     * @return true 影响源, false不是.
     */
    public boolean isSource();

    /**
     * 设置当前参与者为影响源.
     */
    public void source();

}
