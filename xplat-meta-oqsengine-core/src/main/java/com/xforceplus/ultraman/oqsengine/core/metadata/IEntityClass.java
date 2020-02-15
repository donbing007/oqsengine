package com.xforceplus.ultraman.oqsengine.core.metadata;

import java.lang.reflect.Field;
import java.util.List;

public interface IEntityClass {
    /**
     * 获得对象的id
     * @return id
     */
    public Long id();

    /**
     * 获得本对象和父对象的关系 - 从父对象往下描述 - 父对象只有1个。
     * @return 根对象默认为Null，OneToOne为OTO，OneToMany为OTM
     */
    public String relation();

    /**
     * 获得本对象的下一级子对象
     * @return 子对象集合 - 子对象目前不继续往下钻
     */
    public List<IEntityClass> entityClasss();

    /**
     * 本对象的属性信息
     * @return 属性集合
     */
    public List<Field> fields();
}
