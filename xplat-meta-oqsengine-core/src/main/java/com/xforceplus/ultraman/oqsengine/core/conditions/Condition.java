package com.xforceplus.ultraman.oqsengine.core.conditions;

import com.xforceplus.ultraman.oqsengine.core.conditions.interfaces.ICondition;
import com.xforceplus.ultraman.oqsengine.core.tools.StringUtils;

import java.util.Collection;
import java.util.Map;

import static java.util.Objects.isNull;

public class Condition<T, R, Children extends Condition<T, R, Children, Param>, Param> implements ICondition,ISqlSegment {

    private String name;

    private String value;

    private String fieldType;

    /**
     * 构造函数
     */
    public Condition(){

    }

    @Override
    public Object eq(Object column, Object val) {
        return null;
    }

    @Override
    public Object ne(Object column, Object val) {
        return null;
    }

    @Override
    public Object gt(Object column, Object val) {
        return null;
    }

    @Override
    public Object ge(Object column, Object val) {
        return null;
    }

    @Override
    public Object lt(Object column, Object val) {
        return null;
    }

    @Override
    public Object le(Object column, Object val) {
        return null;
    }

    @Override
    public Object between(Object column, Object val1, Object val2) {
        return null;
    }

    @Override
    public Object notBetween(Object column, Object val1, Object val2) {
        return null;
    }

    @Override
    public Object like(Object column, Object val) {
        return null;
    }

    @Override
    public Object notLike(Object column, Object val) {
        return null;
    }

    @Override
    public Object likeLeft(Object column, Object val) {
        return null;
    }

    @Override
    public Object likeRight(Object column, Object val) {
        return null;
    }

    @Override
    public Object groupBy(Object[] columns) {
        return null;
    }

    @Override
    public Object orderBy(boolean isAsc, Object[] columns) {
        return null;
    }

    @Override
    public Object notIn(Object column, Collection coll) {
        return null;
    }

    @Override
    public Object in(Object column, Collection coll) {
        return null;
    }

    @Override
    public String getSqlSegment() {
        return null;
    }
}
