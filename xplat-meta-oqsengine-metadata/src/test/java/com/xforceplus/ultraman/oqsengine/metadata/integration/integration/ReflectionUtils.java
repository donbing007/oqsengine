package com.xforceplus.ultraman.oqsengine.metadata.integration.integration;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
public class ReflectionUtils {

    /**
     * reflection.
     */
    public static void reflectionFieldValue(Collection<Field> fs, String fieldName, Object object, Object fieldValue)
        throws IllegalAccessException {
        boolean valued = false;
        for (Field field : fs) {
            if (fieldName.equals(field.getName())) {
                field.setAccessible(true);
                field.set(object, fieldValue);
                valued = true;
                break;
            }
        }

        if (!valued) {
            throw new IllegalAccessException(String.format("reflectionUtils not set the value cause not found field : [%s]", fieldName));
        }
    }

    /**
     * print.
     */
    public static Collection<Field> printAllMembers(Object obj) {
        Class<?> cls = obj.getClass();
        Map<String, Field> fieldMap = new HashMap<>();
        while (cls != null && cls != Object.class) {
            Field[] fields = cls.getDeclaredFields();

            for (Field fd : fields) {
                fieldMap.putIfAbsent(fd.getName(), fd);
            }

            cls = cls.getSuperclass();
        }

        return fieldMap.values();
    }

}
