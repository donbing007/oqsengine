package com.xforceplus.ultraman.oqsengine.sdk.util;

import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO may add new mutable method
 * request builder
 */
public class RequestBuilder {
    private Integer pageNo;
    private Integer pageSize;
    private ConditionBuilder conditionBuilder = new ConditionBuilder();
    private List<FieldSort> sort = new ArrayList<>();
    private ItemBuilder itemBuilder = new ItemBuilder();

    public RequestBuilder field(String code, ConditionOp op, List<?> values) {
        List<String> strValues = values.stream().map(String::valueOf).collect(Collectors.toList());
        conditionBuilder.field(code, op, strValues);
        return this;
    }

    public RequestBuilder subField(String entityCode, String code, ConditionOp op, List<?> values) {
        List<String> strValues = values.stream().map(String::valueOf).collect(Collectors.toList());
        conditionBuilder.sub(entityCode).subField(code, op, strValues);
        return this;
    }

    public RequestBuilder field(String code, ConditionOp op, Object... values) {
        List<String> strValues = Arrays.stream(values).map(String::valueOf).collect(Collectors.toList());
        conditionBuilder.field(code, op, strValues);
        return this;
    }

    public RequestBuilder subField(String entityCode, String code, ConditionOp op, Object... values) {
        List<String> strValues = Arrays.stream(values).map(String::valueOf).collect(Collectors.toList());
        conditionBuilder.sub(entityCode).subField(code, op, strValues);
        return this;
    }

    public RequestBuilder item(List<String> fields) {
        String[] fieldArr = fields.toArray(new String[fields.size()]);
        itemBuilder.item(fieldArr);
        return this;
    }

    public RequestBuilder subItem(String entityCode, List<String> fields) {
        String[] fieldArr = fields.toArray(new String[fields.size()]);
        itemBuilder.sub(entityCode).subItem(fieldArr);
        return this;
    }

    public RequestBuilder item(String... fields) {
        itemBuilder.item(fields);
        return this;
    }

    public RequestBuilder subItem(String entityCode, String... fields) {
        itemBuilder.sub(entityCode).subItem(fields);
        return this;
    }

    public RequestBuilder pageNo(Integer pageNo) {
        this.pageNo = pageNo;
        return this;
    }

    public RequestBuilder pageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public RequestBuilder sort(String field, String order) {
        FieldSort fieldSort = new FieldSort();
        fieldSort.setField(field);
        fieldSort.setOrder(order);
        sort.add(fieldSort);
        return this;
    }

    public ConditionQueryRequest build() {
        ConditionQueryRequest request = new ConditionQueryRequest();
        request.setPageNo(pageNo);
        request.setPageSize(pageSize);
        request.setConditions(conditionBuilder.build());
        request.setSort(sort);
        request.setEntity(itemBuilder.build());
        return request;
    }

    private class ConditionBuilder {
        private List<FieldCondition> fieldConditions = new ArrayList<>();
        private Map<String, SubFieldCondition> subFieldConditions = new HashMap<>();
        private SubFieldCondition subFieldCondition;

        public void field(String code, ConditionOp op, List<String> values) {
            FieldCondition fieldCondition = new FieldCondition();
            fieldCondition.setCode(code);
            fieldCondition.setOperation(op);
            fieldCondition.setValue(values);
            fieldConditions.add(fieldCondition);
        }

        public ConditionBuilder sub(String code) {
            if (subFieldConditions.containsKey(code)) {
                subFieldCondition = subFieldConditions.get(code);
            } else {
                subFieldCondition = new SubFieldCondition();
                subFieldCondition.setCode(code);
                subFieldCondition.setFields(new ArrayList<>());
                subFieldConditions.put(code, subFieldCondition);
            }
            return this;
        }

        public void subField(String code, ConditionOp op, List<String> values) {
            FieldCondition fieldCondition = new FieldCondition();
            fieldCondition.setCode(code);
            fieldCondition.setOperation(op);
            fieldCondition.setValue(values);
            subFieldCondition.getFields().add(fieldCondition);
        }

        public Conditions build() {
            Conditions conditions = new Conditions();
            conditions.setFields(fieldConditions);
            conditions.setEntities(new ArrayList<>(subFieldConditions.values()));
            return conditions;
        }
    }

    private class ItemBuilder {
        private List<String> fields = new ArrayList<>();
        private Map<String, SubEntityItem> entities = new HashMap<>();
        private SubEntityItem subEntityItem;

        public void item(String... fields) {
            this.fields.addAll(Arrays.asList(fields));
        }

        public ItemBuilder sub(String code) {
            if (entities.containsKey(code)) {
                subEntityItem = entities.get(code);
            } else {
                subEntityItem = new SubEntityItem();
                subEntityItem.setCode(code);
                subEntityItem.setFields(new ArrayList<>());
                entities.put(code, subEntityItem);
            }
            return this;
        }

        public void subItem(String... fields) {
            subEntityItem.getFields().addAll(Arrays.asList(fields));
        }

        public EntityItem build() {
            EntityItem entityItem = new EntityItem();
            entityItem.setFields(fields);
            entityItem.setEntities(new ArrayList<>(entities.values()));
            return entityItem;
        }
    }
}
