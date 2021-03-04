package com.xforceplus.ultraman.oqsengine.changelog.utils;

import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeValue;
import com.xforceplus.ultraman.oqsengine.changelog.domain.Changelog;
import com.xforceplus.ultraman.oqsengine.changelog.domain.HistoryValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.oqs.OqsRelation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * changelog helper
 */
public class ChangelogHelper {

    /**
     * changelog id
     * @param changelogList
     * @return
     */
    public static Map<Long, List<ChangeValue>> getMappedValue(List<Changelog> changelogList){
        Map<Long, List<ChangeValue>> valueMapping = changelogList.stream()
                .flatMap(x -> x.getChangeValues().stream())
                .collect(Collectors.groupingBy(ChangeValue::getFieldId));
       return valueMapping;
    }

    /**
     * changelog id
     * @param changelogList
     * @return
     */
    public static Map<Long, List<HistoryValue>> getMappedHistoryValue(List<Changelog> changelogList){
        Map<Long, List<HistoryValue>> history = changelogList.stream()
                .flatMap(x -> x.getChangeValues().stream().map(cv -> {
                    HistoryValue historyValue = new HistoryValue();
                    historyValue.setFieldId(cv.getFieldId());
                    historyValue.setCommitId(x.getVersion());
                    historyValue.setValue(cv);
                    return historyValue;
                }))
                .collect(Collectors.groupingBy(HistoryValue::getFieldId));
        return history;
    }

    /**
     * check if has a fieldOwner
     * @param relation
     * @return
     */
    public static boolean isReferenceSet(OqsRelation relation){
        return relation.getRelOwnerClassId() != relation.getFieldOwner();
    }

//    /**
//     * merge changelog to one value
//     * @param changeValues
//     * @return
//     */
//    public static List<String> mergeChangelog(List<ChangeValue> changeValues){
//        Optional<ChangeValue> firstSet = changeValues.stream().filter(x -> x.getOp() == ChangeValue.Op.SET).findFirst();
//
//        List<String> values = new ArrayList<>();
//
//        int index = changeValues.size() - 1;
//
//        /**
//         * set base
//         */
//        if(firstSet.isPresent()){
//            values.addAll(firstSet.get().getValues());
//            index = changeValues.indexOf(firstSet.get()) - 1;
//        }
//
//        for(int i = index; i >= 0 ; i --){
//            ChangeValue changeValue = changeValues.get(i);
//            if(changeValue != null){
//                List<String> safeValues = Optional.ofNullable(changeValue.getValues()).orElse(Collections.emptyList());
//                switch(changeValue.getOp()){
//                    case ADD:
//                        values.addAll(safeValues);
//                        break;
//                    case DEL:
//                        values.removeAll(safeValues);
//                        break;
//                    default:
//
//                }
//            }
//        }
//
//        return values;
//    }

    /**
     * serialize to String
     * @param value
     * @return
     */
    public static String serialize(IValue value){
        if(value instanceof StringValue
                || value instanceof StringsValue
                || value instanceof DecimalValue
                || value instanceof EnumValue
                || value instanceof BooleanValue
        ){
            return value.valueToString();
        } else if(value instanceof LongValue
                || value instanceof DateTimeValue) {
            return Long.toString(value.valueToLong());
        } else {
            return value.valueToString();
        }
    }

    /**
     * deserialize string to ivalue
     * @param rawValue
     * @param entityField
     * @return
     */
    public static IValue deserialize(String rawValue, IEntityField entityField){
        IValue retValue = null;

        if(rawValue == null ){
            return null;
        }

        switch (entityField.type()) {
            case STRING:
                retValue = new StringValue(entityField, rawValue);
                break;
            case STRINGS:
                retValue = new StringsValue(entityField, rawValue.split(","));
                break;
            case BOOLEAN:
                retValue = new BooleanValue(entityField, Boolean.parseBoolean(rawValue));
                break;
            case DATETIME:
                long timestamp = Long.parseLong(rawValue);
                LocalDateTime time =
                        LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                                DateTimeValue.zoneId);
                retValue = new DateTimeValue(entityField, time);
                break;
            case ENUM:
                retValue = new EnumValue(entityField, rawValue);
                break;
            case LONG:
                retValue = new LongValue(entityField, Long.parseLong(rawValue));
                break;
            case DECIMAL:
                retValue = new DecimalValue(entityField, new BigDecimal(rawValue));
                break;
            default:
        }

        return retValue;
    }
}
