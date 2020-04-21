package com.xforceplus.ultraman.oqsengine.sdk.vo.dto;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {
 *      *         pageNo: number,
 *      *         pageSize: number,
 *      *         conditions: {
 *      *             fields: [
 *      *                 {
 *      *                     code: string,
 *      *                     operation: enum{equal, like, in, gteq&lteq, gteq&lt, gt&lteq, gt&lt, gt, gteq, lt, lteq},
 *      *                     value: Array
 *      *                 }
 *      *             ],
 *      *             entities: [
 *      *                 {
 *      *                     code: 'otherEntity',
 *      *                     fields: [
 *      *                         {
 *      *                             code: string,
 *      *                             operation: enum{equal, like, in, gteq&lteq, gteq&lt, gt&lteq, gt&lt, gt, gteq, lt, lteq},
 *      *                             value: Array
 *      *                         }
 *      *                     ],
 *      *                 }
 *      *             ]
 *      *         },
 *      *         sort: [
 *      *             {
 *      *                 field: string,
 *      *                 order: enum{asc, desc}
 *      *             }
 *      *         ],
 *      *         entity: {
 *      *             fields: ['id', 'name', 'field1', 'field2', 'field3'],
 *      *             entities: [
 *      *                 {
 *      *                     code: 'otherEntity1',
 *      *                     fields: ['name'],
 *      *                 },E
 *      *                 {
 *      *                     code: 'otherEntity2',
 *      *                     fields: ['name'],
 *      *                 },
 *      *             ],
 *      *         },
 *      *     }
 */
public class ConditionQueryRequest {

    private Integer pageNo;

    private Integer pageSize;

    private Conditions conditions;

    private List<FieldSort> sort;

    //return code
    private EntityItem entity;

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Conditions getConditions() {
        return conditions;
    }

    public void setConditions(Conditions conditions) {
        this.conditions = conditions;
    }

    public List<FieldSort> getSort() {
        return sort;
    }

    public void setSort(List<FieldSort> sort) {
        this.sort = sort;
    }

    public EntityItem getEntity() {
        return entity;
    }

    public void setEntity(EntityItem entity) {
        this.entity = entity;
    }

    public Set<String> getStringKeys(){
        return Optional.ofNullable(entity).map(x -> {
            Stream<String> stream = x.getFields()
                    .stream();
            Stream<String> keyStream = x.getEntities()
                    .stream().flatMap(sub -> sub.getFields().stream().map(subField -> sub.getCode() + "." + subField));
            return Stream.concat(stream, keyStream).collect(Collectors.toSet());
        }).orElseGet(Collections::emptySet);
    }
}
