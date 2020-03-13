package com.xforceplus.ultraman.oqsengine.sdk.handler;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

/**
 * Entity公共字段数据填充处理类
 *
 * @author wangzheng
 * @since 2020-03-05
 */
public class EntityMetaHandler {

    /**
     * 由于系统字段未打上标记，这里做简化处理，预先在代码中设定系统字段。
     * 创建的时候需要操作的字段集合
     */
    private static final String[] insertFields = {"tenant_id","create_time","create_user","create_user_name","delete_flag","update_time","update_user","update_user_name"};
    /**
     * 更新的时候需要操作的字段集合
     */
    private static final String[] updateFields = {"update_time","update_user","update_user_name"};

    /**
     * 保存对象字段填充
     * @param entityClass 对象
     * @param body 数据对象
     * @return body
     */
    public Map<String, Object> insertFill(EntityClass entityClass, Map<String, Object> body){
        for (String insertField : insertFields) {
            Object o = this.getFieldValByName(entityClass,body,insertField);
            if (null == o){
                if (insertField.equals("tenant_id")){
//                    setFieldValByName(entityClass,body,insertField,1);
                }else if (insertField.equals("create_time")){
                    setFieldValByName(entityClass,body,insertField,LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli());
                }else if (insertField.equals("create_user")){
//                    setFieldValByName(entityClass,body,insertField,1);
                }else if (insertField.equals("create_user_name")){
//                    setFieldValByName(entityClass,body,insertField,1);
                }else if (insertField.equals("delete_flag")){
                    setFieldValByName(entityClass,body,insertField,"1");
                }else if (insertField.equals("update_time")){
                    setFieldValByName(entityClass,body,insertField,LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli());
                }else if (insertField.equals("update_user")){
//                    setFieldValByName(entityClass,body,insertField,1);
                }else if (insertField.equals("update_user_name")){
//                    setFieldValByName(entityClass,body,insertField,1);
                }
            }
        }
        return body;
    };

    /**
     * 更新对象字段填充
     * @param entityClass
     * @param body
     * @return body
     */
    public Map<String, Object> updateFill(EntityClass entityClass, Map<String, Object> body){
        for (String updateField : updateFields) {
            Object o = this.getFieldValByName(entityClass,body,updateField);
            if (null == o){
                if (updateField.equals("update_time")){
                    setFieldValByName(entityClass,body,updateField,LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli());
                }else if (updateField.equals("update_user")){
//                    setFieldValByName(entityClass,body,updateField,1);
                }else if (updateField.equals("update_user_name")){
//                    setFieldValByName(entityClass,body,updateField,1);
                }
            }
        }
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
        Optional<IEntityField> entityField = entityClass.fields().stream()
                .filter(f -> f.name().equals(fieldName))
                .findFirst();
        if (entityField.isPresent()){
            return true;
        }
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

        if (isFill(entityClass,fieldName,fieldVal)){
            body.put(fieldName,fieldVal);
        }
        return body;
    }

}
