package com.xforceplus.ultraman.oqsengine.sdk.handler;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.xplat.galaxy.framework.context.ContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

import static com.xforceplus.xplat.galaxy.framework.context.ContextKeys.LongKeys.ID;
import static com.xforceplus.xplat.galaxy.framework.context.ContextKeys.StringKeys.*;

/**
 * Entity公共字段数据填充处理类
 *
 * @author wangzheng
 * @since 2020-03-05
 */
public class EntityMetaHandler {

    @Autowired
    private ContextService contextService;

    /**
     * 由于系统字段未打上标记，这里做简化处理，预先在代码中设定系统字段。
     * 创建的时候需要操作的字段集合
     */
    private static final String[] insertFields = {"tenant_id","create_time","create_user_id","create_user_name","delete_flag","update_time","update_user_id","update_user_name"};
    /**
     * 更新的时候需要操作的字段集合
     */
    private static final String[] updateFields = {"update_time","update_user_id","update_user_name"};

    /**
     * 保存对象字段填充
     * @param entityClass 对象
     * @param body 数据对象
     * @return body
     */
    public Map<String, Object> insertFill(IEntityClass entityClass, Map<String, Object> body){
        for (String insertField : insertFields) {
            Object o = this.getFieldValByName(entityClass,body,insertField);
            if (null == o){

                if (insertField.equals("tenant_id")){
                    String tenantId = contextService.get(TENANTID_KEY);
                    if (!StringUtils.isEmpty(tenantId)) {
                        setFieldValByName(entityClass,body,insertField,tenantId);
                    }
                }else if (insertField.equals("create_time")){
                    setFieldValByName(entityClass,body,insertField,LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli());
                }else if (insertField.equals("create_user_id")){
                    Long userId = contextService.get(ID);
                    if (userId != null) {
                        setFieldValByName(entityClass,body,insertField,userId);
                    }
                }else if (insertField.equals("create_user_name")){
                    String userDisplayName = contextService.get(USER_DISPLAYNAME);
                    if (!StringUtils.isEmpty(userDisplayName)) {
                        setFieldValByName(entityClass,body,insertField,userDisplayName);
                    }
                }else if (insertField.equals("delete_flag")){
                    setFieldValByName(entityClass,body,insertField,"1");
                }else if (insertField.equals("update_time")){
                    setFieldValByName(entityClass,body,insertField,LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli());
                }else if (insertField.equals("update_user_id")){
                    Long userId = contextService.get(ID);
                    if (userId != null) {
                        setFieldValByName(entityClass,body,insertField,userId);
                    }
                }else if (insertField.equals("update_user_name")){
                    String userDisplayName = contextService.get(USER_DISPLAYNAME);
                    if (!StringUtils.isEmpty(userDisplayName)) {
                        setFieldValByName(entityClass,body,insertField,userDisplayName);
                    }
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
                }else if (updateField.equals("update_user_id")){
                    Long userId = contextService.get(ID);
                    if (userId != null) {
                        setFieldValByName(entityClass,body,updateField,userId);
                    }
                }else if (updateField.equals("update_user_name")){
                    String userDisplayName = contextService.get(USER_DISPLAYNAME);
                    if (!StringUtils.isEmpty(userDisplayName)) {
                        setFieldValByName(entityClass,body,updateField,userDisplayName);
                    }
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
    public boolean isFill(IEntityClass entityClass,String fieldName,Object fieldVal){
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
    public Object getFieldValByName(IEntityClass entityClass, Map<String, Object> body,String fieldName) {
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
    public Map<String, Object> setFieldValByName(IEntityClass entityClass, Map<String, Object> body,String fieldName, Object fieldVal) {

        if (isFill(entityClass,fieldName,fieldVal)){
            body.put(fieldName,fieldVal);
        }
        return body;
    }

}
