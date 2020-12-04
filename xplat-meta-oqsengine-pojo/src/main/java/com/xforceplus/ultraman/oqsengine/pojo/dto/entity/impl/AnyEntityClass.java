package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 这是一个特殊的 EntityClass,类似于 JAVA 中 Object.class.
 * 可以表示任意的类型,但其中所有的属性都无意义.
 *
 * @author dongbin
 * @version 0.1 2020/3/1 23:46
 * @since 1.8
 */
public class AnyEntityClass implements IEntityClass {

    private static final IEntityClass INSTANCE = new AnyEntityClass();

    /**
     * 获取实例.
     * @return 实例.
     */
    public static IEntityClass getInstance() {
        return INSTANCE;
    }

    @Override
    public long id() {
        return -1;
    }

    @Override
    public String code() {
        return "";
    }

    @Override
    public String name() {
        return "";
    }

    @Override
    public Collection<Relation> relations() {
        return Collections.emptyList();
    }

    @Override
    public Collection<IEntityClass> entityClasss() {
        return Collections.emptyList();
    }

    @Override
    public IEntityClass extendEntityClass() {
        return null;
    }

    @Override
    public List<IEntityField> fields() {
        return Collections.emptyList();
    }

    @Override
    public Optional<IEntityField> field(String name) {
        return Optional.empty();
    }

    @Override
    public Optional<IEntityField> field(long id) {
        return Optional.empty();
    }

    @Override
    public boolean isAny() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return AnyEntityClass.class.isInstance(obj);
    }
}
