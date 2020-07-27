package com.xforceplus.ultraman.oqsengine.sdk.util;

import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author admin
 */
public class ConditionQueryRequestHelper {

    public static ConditionQueryRequest build(List<Long> ids, ConditionQueryRequest condition) {

        //do nothing
        if (ids == null) {
            return condition;
        }

        ConditionQueryRequest newRequest = copyOrNew(condition);

        Conditions conditions = newRequest.getConditions();

        FieldCondition idCondition = new FieldCondition();
        idCondition.setCode("id");
        idCondition.setOperation(ConditionOp.in);
        idCondition.setValue(ids.stream().map(Object::toString).collect(Collectors.toList()));

        if (conditions == null) {
            //new one with ids
            List<FieldCondition> fieldConditions = Collections.singletonList(idCondition);
            Conditions newConditions = new Conditions();
            newConditions.setFields(fieldConditions);
            newRequest.setConditions(newConditions);
        } else {
            List<FieldCondition> fields = conditions.getFields();
            List<FieldCondition> fieldConditions = Optional.ofNullable(fields)
                    .map(LinkedList::new).orElseGet(LinkedList::new);
            fieldConditions.add(idCondition);
            conditions.setFields(fieldConditions);
        }

        return newRequest;
    }

    /**
     * not deep one only replace all reference to new one
     *
     * @param condition
     * @return
     */
    public static ConditionQueryRequest copyOrNew(ConditionQueryRequest condition) {

        ConditionQueryRequest result = new ConditionQueryRequest();
        if (condition == null) {
            return result;
        } else {

            Conditions conditions = condition.getConditions();
            if (conditions != null) {
                Conditions newConditions = new Conditions();
                if (conditions.getFields() != null) {
                    newConditions.setFields(new LinkedList<>(conditions.getFields()));
                }

                if (conditions.getEntities() != null) {
                    newConditions.setEntities(new LinkedList<>(conditions.getEntities()));
                }

                result.setConditions(newConditions);
            }

            List<NameMapping> mapping = condition.getMapping();
            if (mapping != null) {
                result.setMapping(new LinkedList<>(mapping));
            }

            List<FieldSort> sort = condition.getSort();
            if (sort != null) {
                result.setSort(new LinkedList<>(sort));
            }

            EntityItem entity = condition.getEntity();
            if (entity != null) {

                EntityItem newItem = new EntityItem();

                List<SubEntityItem> entities = entity.getEntities();
                List<String> fields = entity.getFields();

                if (entities != null) {
                    newItem.setEntities(new LinkedList<>(entities));
                }

                if (fields != null) {
                    newItem.setFields(new LinkedList<>(fields));
                }
                result.setEntity(newItem);
            }

            result.setPageSize(condition.getPageSize());
            result.setPageNo(condition.getPageNo());

            return result;
        }
    }
}
