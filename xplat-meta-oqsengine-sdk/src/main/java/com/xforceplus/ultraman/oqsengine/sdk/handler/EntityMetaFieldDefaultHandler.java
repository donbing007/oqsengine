package com.xforceplus.ultraman.oqsengine.sdk.handler;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.DictMapLocalStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        List<IEntityField> fields = getDefaultFields(entityClass);
        for (IEntityField field : fields) {
            Object o = this.getFieldValByName(entityClass,body,field.name());
            if (null == o){
                setFieldValByName(entityClass, body, field.name(), field.defaultValue());
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

        return body;
    };

    /**
     * 返回对象需要设置默认值的字段
     * @param entityClass
     * @return List<IEntityField>
     */
    public List<IEntityField> getDefaultFields(EntityClass entityClass){
        List<IEntityField> fields = entityClass.fields();
        List<IEntityField> defaultFields = new ArrayList<>();
        fields.forEach(f -> {
            String dictId = f.dictId();
            String defaultValue = f.defaultValue();
            String type = f.type().name();
            if("ENUM".equals(type)){
                if (!StringUtils.isEmpty(dictId) && !StringUtils.isEmpty(defaultValue)){
                    defaultFields.add(f);
                }
            } else if ("LONG".equals(type)){
                if (!StringUtils.isEmpty(defaultValue)){
                    defaultFields.add(f);
                }
            }
        });
        return defaultFields;
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
