package com.xforceplus.ultraman.oqsengine.changelog.utils;

import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeValue;
import com.xforceplus.ultraman.oqsengine.changelog.domain.Changelog;
import com.xforceplus.ultraman.oqsengine.changelog.domain.HistoryValue;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ValueWrapper;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldLikeRelationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsRelation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.*;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IValueUtils;

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
     * check if has a fieldOwner is referenceSet
     * @param relation
     * @return
     */
    public static boolean isReferenceSetInCurrentView(OqsRelation relation, Long entityClassId){
        if(relation.getRelationType() == OqsRelation.RelationType.MANY_TO_ONE){
            return !relation.isBelongToOwner();
        } else if(relation.getRelationType() == OqsRelation.RelationType.ONE_TO_MANY){
            return !relation.isBelongToOwner();
        }

        return false;
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

    public static String serialize(ValueWrapper valueWrapper){
        return serialize(valueWrapper.getIValue());
    }

    /**
     * serialize to String
     * @param value
     * @return
     */
    public static String serialize(IValue value){

        return IValueUtils.serialize(value);
    }

    /**
     * deserialize string to ivalue
     * @param rawValue
     * @param entityField
     * @return
     */
    public static IValue deserialize(String rawValue, IEntityField entityField){
        return IValueUtils.deserialize(rawValue, entityField);
    }

    public static <T> List<T> mergeSortedList(List<T> listA, List<T> listB, Comparator<T> comparator){

        List<T> mergedList = new LinkedList<>();
        int i = 0 ,j = 0;
        for( ; i < listA.size() && j < listB.size() ; ){
            T ta = listA.get(i);
            T tb = listB.get(j);
            if(comparator.compare(ta, tb) >= 0){
                mergedList.add(ta);
                i ++;
            }else{
                mergedList.add(tb);
                j ++;
            };
        }

        if(i < listA.size()){
            mergedList.addAll(listA.subList(i, listA.size() - 1));
        }

        if(j < listB.size()){
            mergedList.addAll(listB.subList(j, listB.size() - 1));
        }

        return mergedList;
    }
}
