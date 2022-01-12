package com.xforceplus.ultraman.oqsengine.storage.pojo;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 多个Entity实例包裹.
 * 会保证加入的顺序.
 * 顺序序号从0开始.
 *
 * @author dongbin
 * @version 0.1 2021/09/15 15:02
 * @since 1.8
 */
public class EntityPackage implements Serializable {

    /**
     * 最大上限.
     */
    public static int MAX_SIZE = 10000;

    private List<Map.Entry<IEntity, IEntityClass>> entities;

    /**
     * 构造新的package实例.
     *
     * @return 新实例.
     */
    public static EntityPackage build() {
        return new EntityPackage();
    }

    /**
     * 包裹中的实例数量.
     *
     * @return 实例数量.
     */
    public int size() {
        if (entities == null) {
            return 0;
        } else {
            return entities.size();
        }
    }

    /**
     * 判断是否已经满.
     * 持有的实例达到上限.
     *
     * @return true 达到上限, false 没有达到.
     */
    public boolean isFull() {
        if (entities == null) {
            return false;
        } else {
            return entities.size() == MAX_SIZE;
        }
    }

    /**
     * 判断是否为空.
     *
     * @return true 为空,false 不为空.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * 包裹中增加一个新的IEntity实例.
     *
     * @param entity 实例.
     */
    public EntityPackage put(IEntity entity, IEntityClass entityClass) {
        if (entity == null) {
            throw new NullPointerException("The target IEntity instance is not valid.");
        }

        if (entityClass == null) {
            throw new NullPointerException("The target EntityClass instance is not vaild.");
        }

        if (!entity.entityClassRef().equals(entityClass.ref())) {
            throw new IllegalArgumentException(
                "The type declared by the current instance does not match the specified type.");
        }

        lazyInit();

        if (entities.size() == MAX_SIZE) {
            throw new IllegalStateException(String.format("The maximum number of packages is %d.", MAX_SIZE));
        }

        entities.add(new AbstractMap.SimpleEntry<>(entity, entityClass));

        return this;
    }

    /**
     * 得到指定序号的实例信息.
     *
     * @param index 从0开始的序号.
     * @return 实例.
     */
    public Optional<Map.Entry<IEntity, IEntityClass>> get(int index) {
        if (index > this.entities.size() - 1) {
            return Optional.empty();
        }

        if (index < 0) {
            return Optional.empty();
        }

        return Optional.ofNullable(this.entities.get(index));
    }

    /**
     * 不安全的方式查询结果.
     * 有可能返回null.
     *
     * @param index 从0开始的序号.
     * @return 实例.
     */
    public Map.Entry<IEntity, IEntityClass> getNotSafe(int index) {
        if (index < 0) {
            return null;
        }

        if (index > this.entities.size() - 1) {
            return null;
        }

        return this.entities.get(index);
    }

    /**
     * 以流的方式读取包裹中的IEntity实例.
     *
     * @return 实例流.
     */
    public Stream<Map.Entry<IEntity, IEntityClass>> stream() {
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
    public Iterator<Map.Entry<IEntity, IEntityClass>> iterator() {
        if (entities == null) {

            return new Iterator<Map.Entry<IEntity, IEntityClass>>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public Map.Entry<IEntity, IEntityClass> next() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EntityPackage)) {
            return false;
        }
        EntityPackage that = (EntityPackage) o;
        return Objects.equals(entities, that.entities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entities);
    }
}
