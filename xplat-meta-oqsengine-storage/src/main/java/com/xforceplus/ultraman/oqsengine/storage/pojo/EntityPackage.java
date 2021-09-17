package com.xforceplus.ultraman.oqsengine.storage.pojo;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * 多个Entity实例包裹.
 *
 * @author dongbin
 * @version 0.1 2021/09/15 15:02
 * @since 1.8
 */
public class EntityPackage implements Serializable {

    /**
     * 最大上限.
     */
    private static int MAX_SIZE = 10000;

    private List<IEntity> entities;
    private IEntityClass entityClass;

    public EntityPackage(IEntityClass entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * 包裹中增加一个新的IEntity实例.
     *
     * @param entity 实例.
     */
    public void put(IEntity entity) {
        if (entity == null) {
            throw new NullPointerException("The target IEntity instance is not valid.");
        }

        if (entity.entityClassRef().getId() != this.entityClass.id()) {
            throw new IllegalStateException(
                String.format("Not expected Class, expected %d-%s.", this.entityClass.id(), this.entityClass.code()));
        }

        lazyInit();

        if (entities.size() == MAX_SIZE) {
            throw new IllegalStateException(String.format("The maximum number of packages is %d.", MAX_SIZE));
        }

        entities.add(entity);
    }

    public IEntityClass getEntityClass() {
        return entityClass;
    }

    /**
     * 以流的方式读取包裹中的IEntity实例.
     *
     * @return 实例流.
     */
    public Stream<IEntity> stream() {
        if (entities == null) {

            return Stream.empty();

        } else {

            return entities.stream();
        }
    }

    /**
     * 以迭代器的方式读取实例.
     *
     * @return 实例迭代器.
     */
    public Iterator<IEntity> iterator() {
        if (entities == null) {

            return new Iterator<IEntity>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public IEntity next() {
                    return null;
                }
            };
        } else {

            return entities.iterator();
        }
    }

    private void lazyInit() {
        if (entities == null) {
            entities = new ArrayList<>(MAX_SIZE);
        }
    }
}
