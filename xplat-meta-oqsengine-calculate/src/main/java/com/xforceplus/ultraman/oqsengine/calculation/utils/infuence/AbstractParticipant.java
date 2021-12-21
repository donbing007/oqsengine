package com.xforceplus.ultraman.oqsengine.calculation.utils.infuence;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 影响树参与者.
 */
public abstract class AbstractParticipant {
    protected IEntityClass entityClass;
    protected IEntityField field;
    protected Collection<IEntity> affectedEntities;
    protected Object attachment;
    protected Infuence.Node node;
    protected AbstractParticipant pre;

    public IEntityClass getEntityClass() {
        return entityClass;
    }

    public IEntityField getField() {
        return field;
    }

    /**
     * 获得得当前参与者的影响entity列表.
     *
     * @return 影响entity列表.
     */
    public Collection<IEntity> getAffectedEntities() {
        if (affectedEntities == null) {
            return Collections.emptyList();
        } else {
            return affectedEntities;
        }
    }


    /**
     * 增加受影响的实例.
     *
     * @param entity 实例.
     */
    public void addAffectedEntity(IEntity entity) {
        if (affectedEntities == null) {
            this.affectedEntities = new ArrayList<>();
        }

        this.affectedEntities.add(entity);
    }

    /**
     * 删除受影响的实例.
     *
     * @param id 实例标识.
     * @return 删除的实例.
     */
    public Optional<IEntity> removeAffectedEntities(long id) {
        if (affectedEntities == null) {
            return Optional.empty();
        } else {
            AtomicReference<IEntity> targetEntity = new AtomicReference<>();
            this.affectedEntities.removeIf(e -> {
                if (e.id() == id) {
                    targetEntity.set(e);
                    return true;
                } else {
                    return false;
                }
            });

            return Optional.ofNullable(targetEntity.get());
        }
    }

    public Optional<Object> getAttachment() {
        return Optional.ofNullable(attachment);
    }

    /**
     * 获取当前参与者影响的参与者列表.
     */
    public List<AbstractParticipant> getNextParticipants() {
        if (node == null) {
            return Collections.emptyList();
        }
        return node.getChildren().size() > 0 ? node.getChildren().stream().map(Infuence.Node::getParticipant)
                .collect(Collectors.toList()) : Collections.emptyList();
    }


    protected void setNode(Infuence.Node node) {
        this.node = node;
    }

    protected  Infuence.Node getNode() {
        return node;
    }

    /**
     * 上一个参与者.
     */
    public AbstractParticipant getPre() {
        return this.pre;
    }

    public void setPre(AbstractParticipant abstractParticipant) {
        this.pre = abstractParticipant;
    }

}
