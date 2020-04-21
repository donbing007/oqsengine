package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;

/**
 * wrappered trait
 *
 * @param <T>
 */
public interface Wrapped<T> {

    T getOriginObject();

    default T getOrigin() {
        if (Wrapped.class.isAssignableFrom(getOriginObject().getClass())) {
            return ((Wrapped<T>) getOriginObject()).getOrigin();
        }

        return getOriginObject();
    }
}
