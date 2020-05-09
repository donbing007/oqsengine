package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;

import java.util.Collection;
import java.util.Optional;

/**
 * Entity结构对象
 * @author wangzheng
 * @version 1.0 2020/3/26 15:10
 */
public interface IEntityClass {

    /**
     * 获得对象的id
     * @return id
     */
    public long id();

    /**
     * 对象code
     * @return
     */
    public String code();

    /**
     * 关系信息的集合
     * @return 根对象默认为Null，OneToOne为OTO，OneToMany为OTM
     */
    public Collection<Relation> relations();

    /**
     * 获得本对象的下一级子对象
     * @return 子对象集合 - 子对象目前不继续往下钻
     */
    public Collection<IEntityClass> entityClasss();

    /**
     * 获取当前对象继承的对象类型.
     * @return 继承的目标类型
     */
    public IEntityClass extendEntityClass();

    /**
     * 本对象的属性信息
     * @return 属性集合
     */
    public Collection<IEntityField> fields();

    /**
     * 本地对象指定字段的信息.
     * @param name 字段名称.
     * @return 字段信息.
     */
    public Optional<IEntityField> field(String name);

    /**
     * 使用字段 ID 获取属性信息.
     * @param id 属性ID.
     * @return 属性名称.
     */
    public Optional<IEntityField> field(long id);

    /**
     * 判断是否表示一个通用类型,和 JAVA 中 Object.class 相似的概念.
     * @return true 是通用类型,false 不是通用类型.
     */
    public boolean isAny();

    /**
     * 是否是一个动态的类型 默认是
     * @return
     */
    default boolean isDynamic(){
        return true;
    }
}
