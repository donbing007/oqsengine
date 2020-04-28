package com.xforceplus.ultraman.oqsengine.pojo.reader;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldLikeRelationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.AliasField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.ColumnField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;
import com.xforceplus.ultraman.oqsengine.pojo.reader.record.GeneralRecord;
import com.xforceplus.ultraman.oqsengine.pojo.reader.record.Record;
import io.vavr.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.xforceplus.ultraman.oqsengine.pojo.reader.FieldScope.ALL;

/**
 * EntityClass Domain class
 * TODO move more code here
 * an EntityClassReader to handler EntityClass constraints
 * <p>
 * field from
 * entitySelf          a
 * entityParent        a
 * entityRelation      rel
 * entityRelatedField  rel.x
 * related
 * <p>
 * <p>
 * entity do not need know the rel field
 *
 * @author luye
 */
public class IEntityClassReader {

    private Logger logger = LoggerFactory.getLogger(IEntityClassReader.class);
//
//    /**
//     * self id - field mapping including
//     *           self fields
//     *           parent fields
//     *           rel fields (owner-side)
//     */
//    private Map<Long, AliasField> idMappingFields_self;
//
//    /**
//     * related id - field mapping including
//     *           related entityclass fields with their parent fields
//     *           rel fields (non owner-side)
//     */
//    private Map<Long, AliasField> idMappingFields_related;


    private IEntityClass entityClass;

    private Map<Long, AliasField> idMappingFieldsAll;

    private Map<String, List<ColumnField>> codedFields_self;

    private Map<String, List<ColumnField>> codedFields_related;

    private List<ColumnField> allColumn_self;

    private List<ColumnField> allColumn_related;

    private Map<Long, IEntityClass> relatedEntities;

    private static final String FIELD_CODE_AMBIGUOUS = "field [{}] code is ambiguous, return first id [{}]";

    private static final String FIELD_MISSING = "[{}] is not available in EntityClass [{}]";

    private List<AliasField> allFields;

    private IEntityClassReader() {

    }

    //TODO optimize
    public IEntityClassReader(IEntityClass entityClass, IEntityClass... related) {

        this.entityClass = entityClass;

        //self fields and parent fields
        Stream<IEntityField> entityFields = entityClass.fields().stream();
        Stream<IEntityField> entityParentFields = Optional
                .ofNullable(entityClass.extendEntityClass())
                .map(IEntityClass::fields)
                .orElse(Collections.emptyList()).stream();

        //TODO narrow usage?'
        List<IEntityClass> narrowedIEntityClasses = related == null ?
                Collections.emptyList() : Arrays.asList(related);

        /**
         * this allow's duplicated
         * and will filter non-fieldLike relation
         */
        Map<Boolean, List<Relation>> fieldLikeRelation
                = entityClass
                .relations()
                .stream()
                .filter(x -> x.getEntityField() != null)
                .filter(x -> FieldLikeRelationType
                        .from(x.getRelationType()).isPresent())
                .collect(Collectors.groupingBy(x -> {
                    return FieldLikeRelationType.from(x.getRelationType()).get().isOwnerSide();
                }));

        //init related entities mapping
        relatedEntities = entityClass
                .entityClasss()
                .stream()
                .collect(Collectors
                        .toMap(IEntityClass::id, y -> y));

        //buildColumnes
        AtomicInteger index = new AtomicInteger(0);
        //TODO handle multi field
        //convert related field in the form x.x
        Stream<ColumnField> fieldsInRelated = entityClass
                .relations()
                .stream()
                .flatMap(rel -> {

                    //find related iEntityClass
                    IEntityClass relatedEntityClass = relatedEntities.get(rel.getEntityClassId());

                    //TODO
                    Stream<ColumnField> selfStream = relatedEntityClass.fields().stream()
                            .map(field -> new ColumnField(
                                    rel.getName() + "." + field.name()
                                    , field
                            ));

                    Stream<ColumnField> parentStream = Optional
                            .ofNullable(relatedEntityClass.extendEntityClass())
                            .map(IEntityClass::fields)
                            .orElseGet(Collections::emptyList)
                            .stream()
                            .map(field -> new ColumnField(
                                    rel.getName() + "." + field.name()
                                    , field
                            ));

                    return Stream.concat(selfStream, parentStream);
                });

        /**
         * turn every field as a column
         */
        allColumn_self = Stream.concat(
                Stream.concat(entityFields, entityParentFields)
                , Optional.ofNullable(fieldLikeRelation.get(true))
                        .orElseGet(Collections::emptyList)
                        .stream().map(Relation::getEntityField)
        ).map(x -> new ColumnField(x.name(), x))
                .distinct()
                .peek(x -> x.setIndex(index.getAndIncrement()))
                .collect(Collectors.toList());

        allColumn_related = Stream.concat(
                fieldsInRelated
                , Optional.ofNullable(fieldLikeRelation.get(false))
                        .orElseGet(Collections::emptyList)
                        .stream()
                        .map(Relation::getEntityField)
                        .map(x -> new ColumnField(x.name(), x))
        ).distinct()
                .peek(x -> x.setIndex(index.getAndIncrement()))
                .collect(Collectors.toList());

        codedFields_self = allColumn_self.stream().collect(Collectors.groupingBy(IEntityField::name));
        codedFields_related = allColumn_related.stream().collect(Collectors.groupingBy(IEntityField::name));

        //group all field
        Stream<IEntityField> allStream = Stream.concat(allColumn_self.stream(), allColumn_related.stream());

        Map<Long, List<IEntityField>> collect = allStream
                .collect(Collectors.groupingBy(IEntityField::id));

        allFields = collect.entrySet().stream()
                .sorted(Comparator.comparingLong(Map.Entry::getKey)).map(x -> {

                    AliasField field = new AliasField(x.getValue().get(0));
                    x.getValue().stream().map(IEntityField::name).forEach(field::addName);
                    return field;
                }).collect(Collectors.toList());


        idMappingFieldsAll = allFields.stream().collect(Collectors.toMap(AliasField::id, y -> y));
    }


    /**
     * field will has multi name so return AliasField
     *
     * @param id
     * @return
     */
    public Optional<AliasField> field(long id) {
        return field(id, ALL);
    }

    public Optional<AliasField> field(long id, FieldScope scope) {
        return Optional.ofNullable(idMappingFieldsAll.get(id));
    }

    private boolean checkAmbiguous(List<ColumnField> candidates) {
        return candidates.stream()
                .map(ColumnField::originField)
                .distinct()
                .count() > 1;
    }

    /**
     * find field code column
     *
     * @param code
     * @param scope
     * @return
     */
    public Optional<ColumnField> column(String code, FieldScope scope) {

        if (scope == null) {
            scope = ALL;
        }

        List<ColumnField> codeSelectedField = Collections.emptyList();

        switch (scope) {
            case SELF_ONLY:
                codeSelectedField = Optional
                        .ofNullable(codedFields_self.get(code))
                        .orElseGet(Collections::emptyList);
                break;
            case RELATED_ONLY:
                codeSelectedField = Optional
                        .ofNullable(codedFields_related.get(code))
                        .orElseGet(Collections::emptyList);
                break;
            case ALL:
                codeSelectedField = new LinkedList<>();
                codeSelectedField.addAll(Optional.ofNullable(codedFields_self.get(code))
                        .orElseGet(Collections::emptyList));

                codeSelectedField.addAll(Optional.ofNullable(codedFields_related.get(code))
                        .orElseGet(Collections::emptyList));
                break;
            default:
        }

        if (!codeSelectedField.isEmpty() && checkAmbiguous(codeSelectedField)) {
            logger.error(FIELD_CODE_AMBIGUOUS, code, codeSelectedField.get(0));
        }

        return codeSelectedField.isEmpty() ? Optional.empty() : Optional.ofNullable(codeSelectedField.get(0));
    }

    /**
     * only get the first
     *
     * @param code
     * @return
     */
    public Optional<ColumnField> column(String code) {
        return column(code, ALL);
    }

    public List<ColumnField> columns() {
        List<ColumnField> list = new ArrayList<>();
        list.addAll(allColumn_self);
        list.addAll(allColumn_related);
        return list;
    }
//

    /**
     * unmodifiableList
     *
     * @return
     */
    public List<IEntityField> fields() {
        return fields(ALL);
    }

    //
    public List<IEntityField> fields(FieldScope fieldScope) {
        return Collections.unmodifiableList(allFields);
    }

    /**
     * generate a subEntityClassReader switch to subClass
     * @return
     */
//    public IEntityClassReader related(FieldLikeRelationType relationType, String relName){
//        IEntityClassReader subReader = new IEntityClassReader();
//    }

    /**
     * will get all unused key
     *
     * @param map
     * @return
     */
    public Set<String> testBody(Map<String, Object> map) {
        Set<String> inputKeys = map.keySet();
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(codedFields_self.keySet());
        allKeys.addAll(codedFields_related.keySet());
        return inputKeys.stream().filter(x -> !allKeys.contains(x)).collect(Collectors.toSet());
    }

    /**
     * zip field -> value
     * e.g
     * A[a,b,c]
     * relB[a,b,c]
     * body[A.a, A.b. B.c]
     *
     * @param body
     * @return
     */
    public Stream<Tuple2<IEntityField, Object>> zipValue(Map<String, Object> body) {

        //warn error field
        testBody(body)
                .forEach(x -> logger.warn(FIELD_MISSING, x, entityClass.code()));

        Record record = toRecord(body);
        return record.stream();
    }

    /**
     * TODO field always needed
     * currently
     * self + parent column + rel column
     *
     * @return
     */
    public Record toRecord(Map<String, Object> body) {
        //find Column by name

        Set<ColumnField> columns = new HashSet<>();

        //fields
        List<ColumnField> valueColumn = body
                .keySet().stream().map(this::column)
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());

        columns.addAll(valueColumn);
        columns.addAll(allColumn_self);

        Record record = new GeneralRecord(columns);

        valueColumn.forEach(x -> record.set(x, body.get(x.name())));
        return record;
    }

    /**
     * Searchable is consider as only the field is field is ownerside
     *
     * @param key
     * @return
     */
    public Optional<IEntityClass> getSearchableRelatedEntity(String key) {

        return entityClass.relations().stream()
                .filter(x -> FieldLikeRelationType.from(x.getRelationType()).map(FieldLikeRelationType::isOwnerSide)
                        .orElse(false))
                .filter(x -> key.equals(x.getName()))
                .map(x -> relatedEntities.get(x.getEntityClassId()))
                .findFirst();
    }

    /**
     * get relation field as origin field
     *
     * @param entityField
     * @return
     */
    public Optional<IEntityField> getRelatedOriginalField(IEntityField entityField){
        String fieldName = entityField.name();
        String[] fields = fieldName.split("\\.");
        if (fields.length > 1){
            String relName = fields[0];
            return column(relName + ".id").map(ColumnField::originField);
        } else {
            return Optional.empty();
        }
    }
}
