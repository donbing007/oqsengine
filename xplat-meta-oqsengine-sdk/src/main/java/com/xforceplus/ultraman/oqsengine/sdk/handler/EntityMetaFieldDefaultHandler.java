package com.xforceplus.ultraman.oqsengine.sdk.handler;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;

import java.util.Map;

/**
 * Entity默认字段数据填充处理类
 *
 * @author wangzheng
 * @since 2020-03-10
 */
public class EntityMetaFieldDefaultHandler {

    /**
     * 保存对象字段填充
     * @param entityClass 对象
     * @param body 数据对象
     * @return body
     */
    public Map<String, Object> insertFill(EntityClass entityClass, Map<String, Object> body){

        return body;
    };

    /**
     * 更新对象字段填充
     * @param entityClass
     * @param body
     * @return body
     */
    public Map<String, Object> updateFill(EntityClass entityClass, Map<String, Object> body){

        return body;
    };

    /**
     * 删除对象字段填充
     * @param entityClass
     * @param body
     * @return body
     */
    public Map<String, Object> deleteFill(EntityClass entityClass, Map<String, Object> body){

        return body;
    };

    /**
     * 判断是否可以填充数据
     * @param entityClass
     * @param fieldName
     * @param fieldVal
     * @return
     */
    public boolean isFill(EntityClass entityClass,String fieldName,Object fieldVal){

        return false;
    }

    /**
     * 根据字段名字获取该字段的值
     * @param entityClass
     * @param body
     * @param fieldName
     * @return
     */
    public Object getFieldValByName(EntityClass entityClass, Map<String, Object> body,String fieldName) {
        if (body.size() == 0 ) {
            return null;
        }
        body.entrySet().stream().map(entry -> {
            if (entry.getKey().equals(fieldName)){
                if (isFill(entityClass,fieldName,entry.getValue())){
                    return entry.getValue();
                }
            }
            return null;
        });
        return null;
    }

    /**
     * 设置该字段的值
     * @param entityClass
     * @param body
     * @param fieldName
     * @param fieldVal
     * @return
     */
    public Map<String, Object> setFieldValByName(EntityClass entityClass, Map<String, Object> body,String fieldName, Object fieldVal) {
        body.entrySet().stream().map(entry -> {
            if (entry.getKey().equals(fieldName)){
                if (isFill(entityClass,fieldName,entry.getValue())){
                    return entry.getValue();
                }
            }
            return null;
        });
        return body;
    }

}
