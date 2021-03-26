package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsRelation;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Entity结构对象
 *
 * @author wangzheng
 * @version 1.0 2020/3/26 15:10
 */
public interface IEntityClass {

    /**
     * 获得对象的id
     *
     * @return id
     */
    long id();

    /**
     * 对象code
     *
     * @return
     */
    String code();

    /**
     * 元信息名称.
     *
     * @return
     */
    String name();

    /**
     * 元信息版本.0为初始版本号.
     *
     * @return 当前元信息版本号.
     */
    int version();

    /**
     * 元信息在继承树中的层级,从0开始.
     *
     * @return 层级.
     */
    int level();

    /**
     * 关系信息的集合
     *
     * @return 根对象默认为Null，OneToOne为OTO，OneToMany为OTM
     */
    Collection<Relation> relations();

    /**
     * 关系信息的集合(oqs内部使用)
     *
     * @return 根对象默认为Null，OneToOne为OTO，OneToMany为OTM
     */
    Collection<OqsRelation> oqsRelations();

    /**
     * 获得本对象的关联对象
     *
     * @return 关联子对象, 最多一层.
     */
    Collection<IEntityClass> relationsEntityClasss();

    /**
     * 获取当前的父对象元信息.
     *
     * @return 父对象元信息.
     */
    Optional<IEntityClass> father();

    /**
     * 获取家族信息.
     *
     * @return 家族列表.顺序从祖先到子孙顺序.
     */
    Collection<IEntityClass> family();

    /**
     * 本对象的属性信息
     *
     * @return 字段列表.
     */
    Collection<IEntityField> fields();

    /**
     * 本对象的字段信息列表.
     *
     * @param filter 过滤器.
     * @return 字段列表.
     */
    default Collection<IEntityField> fields(Predicate<? super IEntityField> filter) {
        return fields().stream().filter(filter).collect(Collectors.toList());
    }

    /**
     * 本地对象指定字段的信息.
     *
     * @param name 字段名称.
     * @return 字段信息.
     */
    Optional<IEntityField> field(String name);

    /**
     * 使用字段 ID 获取属性信息.
     *
     * @param id 属性ID.
     * @return 属性名称.
     */
    Optional<IEntityField> field(long id);

    /**
     * 获得当前元信息的指针.
     *
     * @return 指针.
     */
    default EntityClassRef ref() {
        return EntityClassRef.Builder.anEntityClassRef()
            .withEntityClassId(id())
            .withEntityClassCode(code())
            .build();
    }

    /**
     * 判断是否表示一个通用类型,和 JAVA 中 Object.class 相似的概念.
     *
     * @return true 是通用类型,false 不是通用类型.
     */
    default boolean isAny() {
        return false;
    }

    /**
     * 是否是一个动态的类型 默认是
     *
     * @return
     */
    default boolean isDynamic() {
        return true;
    }
}
