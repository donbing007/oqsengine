package com.xforceplus.ultraman.oqsengine.storage.master.unique.impl;

import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.select.BusinessKey;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.master.unique.UniqueIndex;
import com.xforceplus.ultraman.oqsengine.storage.master.unique.UniqueIndexValue;
import com.xforceplus.ultraman.oqsengine.storage.master.unique.UniqueKeyGenerator;
import io.micrometer.core.instrument.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 2020/10/19 7:38 PM
 */
public class SimpleFieldKeyGenerator implements UniqueKeyGenerator {

    @Resource
    private MetaManager metaManager;

    private static final Logger logger = LoggerFactory.getLogger(SimpleFieldKeyGenerator.class);
    private static final int KEY_MAX_LENGTH = 256;
    private static final int UNIQUE_CONFIG_ARRAY_SIZE = 3;

    @Override
    public Map<String, UniqueIndexValue> generator(IEntity entity) {
       Optional<IEntityClass> entityClass =  metaManager.load(entity.entityClassRef().getId());
       if (!entityClass.isPresent()) {
           throw new RuntimeException(String.format("Can not find andy EntityClass with id %s", entity.id()));
       }
        Map<String, UniqueIndex> uniqueMap = getUniqueIndexMap(entityClass.get());
        return getUniqueIndexValueMap(uniqueMap, s -> {
            Optional<IValue> fieldValue = entity.entityValue().getValue(s);
            return fieldValue.isPresent() ? fieldValue.get().getValue() : "";
        });
    }

    @Override
    public Map<String, UniqueIndexValue> generator(List<BusinessKey> businessKeys, IEntityClass entityClass) {
//        Optional<IEntityClass> entityClass = metaManager.load(entityClassRef.getId());
//        if (!entityClass.isPresent()) {
//            throw new RuntimeException(String.format("Can not find andy EntityClass with id %s", entityClassRef.getId()));
//        }
        Map<String, UniqueIndex> uniqueMap = getUniqueIndexMap(entityClass);
        Map<String, Object> businessKeyMap = businessKeys.stream().collect(Collectors.toMap(BusinessKey::getFieldName, BusinessKey::getValue));
        return getUniqueIndexValueMap(uniqueMap, s -> {
            Object fieldValue = businessKeyMap.get(s);
            return fieldValue != null ? fieldValue : "";
        });
    }

    private Map<String, UniqueIndexValue> getUniqueIndexValueMap(Map<String, UniqueIndex> uniqueMap, Function<String, Object> fieldValueFun) {
        Map<String, UniqueIndexValue> keys = new LinkedHashMap<>();
        uniqueMap.entrySet().stream().forEach(e -> {
            StringBuilder keyBuilder = new StringBuilder();
            int i = 0;
            List<SortedEntityField> fields = new ArrayList<>(e.getValue().getFields());
            fields.sort(Comparator.comparingInt(SortedEntityField::getSort));
            for (SortedEntityField field : fields) {
                Object fieldValue = fieldValueFun.apply(field.getField().name());
                keyBuilder.append(fieldValue);
                if (i < e.getValue().getFields().size() - 1) {
                    keyBuilder.append("-");
                }
                i++;
            }
            String key = keyBuilder.toString();
            if (key.length() > KEY_MAX_LENGTH) {
                throw new RuntimeException(String.format("Unique Key is too long! must less than %s,your key is %s", KEY_MAX_LENGTH, key));
            }
            UniqueIndexValue value = UniqueIndexValue.builder()
                    .code(e.getValue().getCode())
                    .name(e.getKey())
                    .value(keyBuilder.toString()).build();
            keys.put(e.getKey(), value);
            if (logger.isDebugEnabled()) {
                logger.debug("UNIQUE KEY : {},value: {}", e.getKey(), value);
            }
        });
        return keys;
    }

    private Map<String, UniqueIndex> getUniqueIndexMap(IEntityClass entityClass) {
        Map<String, UniqueIndex> uniqueMap = new LinkedHashMap<>();
        List<IEntityField> totalFields = new ArrayList<>(entityClass.fields());
        Optional<IEntityClass> tmp = entityClass.father();
        while (tmp.isPresent()) {
            totalFields.addAll(tmp.get().fields());
            tmp = tmp.get().father();
        }
        totalFields.stream()
                .filter(item -> !StringUtils.isBlank(item.config().getUniqueName()))
                .forEach(item -> {
                    // uniqueName format : "code:name:sort,code:name:sort"
                    String uniqueName = item.config().getUniqueName();
                    Arrays.stream(uniqueName.split(",")).forEach(u -> {
                        String[] keys = u.split(":");
                        if (keys.length != UNIQUE_CONFIG_ARRAY_SIZE) {
                            throw new RuntimeException("uniqueName config format is not correct! it must be this format for example : code:name:sort");
                        }
                        String code = keys[0];
                        String name = keys[1];
                        int sort = Integer.valueOf(keys[2]);
                        if (uniqueMap.containsKey(name)) {
                            SortedEntityField field = new SortedEntityField(item, sort);
                            uniqueMap.get(name).add(field);
                        } else {
                            List<SortedEntityField> fields = new ArrayList<>();
                            SortedEntityField field = new SortedEntityField(item, sort);
                            fields.add(field);
                            UniqueIndex index = new UniqueIndex();
                            index.setName(name);
                            index.setCode(code);
                            index.setFields(fields);
                            uniqueMap.put(name, index);
                        }
                    });
                });
        return uniqueMap;
    }
}
