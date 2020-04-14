package com.xforceplus.ultraman.oqsengine.pojo.reader;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldLikeRelationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IEntityFieldHelper;
import com.xforceplus.ultraman.oqsengine.pojo.utils.OptionalHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.xforceplus.ultraman.oqsengine.pojo.reader.FieldScope.ALL;

/**
 * EntityClass Domain class
 * TODO move more code here
 * an EntityClassReader to handler EntityClass constraints
 * @author luye
 */
public class IEntityClassReader {

    Logger logger = LoggerFactory.getLogger(IEntityClassReader.class);

    private Map<Long, List<IEntityField>> idMappingFields_self;

    private Map<Long, List<IEntityField>> idMappingFields_related;

    private Map<String, List<IEntityField>> codedFields_self;

    private Map<String, List<IEntityField>> codedFields_related;

    private List<IEntityField> allFields_self;

    private List<IEntityField> allFields_related;

    private Map<Long, IEntityClass> relatedEntities;

    private static String FIELD_CODE_AMBIGUOUS = "field [{}] code is ambiguous, return first id [{}]";


    public IEntityClassReader(IEntityClass entityClass, IEntityClass ... related){

        //self fields and parent fields
        Stream<IEntityField> entityFields = entityClass.fields().stream();
        Stream<IEntityField> entityParentFields = Optional
                .ofNullable(entityClass.extendEntityClass())
                .map(IEntityClass::fields)
                .orElse(Collections.emptyList()).stream();



        //TODO narrow usage?
        List<IEntityClass> narrowedIEntityClasses = related == null ? Collections.emptyList() : Arrays.asList(related);

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

        //TODO handle multi field
        //convert related field in the form x.x
        Stream<IEntityField> fieldsInRelated = entityClass
            .relations()
            .stream()
            .flatMap(rel -> {

                IEntityClass iEntityClass = relatedEntities.get(rel.getEntityClassId());

                //TODO
                Stream<IEntityField> selfStream = x.fields().stream()
                        .map(field -> IEntityFieldHelper.withName(field
                                , x.code() + "." + field.name()));

                Stream<IEntityField> parentStream = Optional.ofNullable(x.extendEntityClass())
                        .map(IEntityClass::fields)
                        .orElseGet(Collections::emptyList)
                        .stream()
                        .map(field -> IEntityFieldHelper.withName(field
                                , x.code() + "." + field.name()));

            return Stream.concat(selfStream, parentStream);
        });;

        allFields_self = Stream.concat(
                Stream.concat(entityFields, entityParentFields)
                , Optional.ofNullable(fieldLikeRelation.get(true))
                        .orElseGet(Collections::emptyList)
                        .stream().map(Relation::getEntityField)
        ).collect(Collectors.toList());

        allFields_related = Stream.concat(
                fieldsInRelated
                , Optional.ofNullable(fieldLikeRelation.get(false))
                        .orElseGet(Collections::emptyList)
                        .stream().map(Relation::getEntityField)
        ).collect(Collectors.toList());

        idMappingFields_self = allFields_self.stream().collect(Collectors.groupingBy(IEntityField::id));
        idMappingFields_related = allFields_related.stream().collect(Collectors.groupingBy(IEntityField::id));

        codedFields_self = allFields_self.stream().collect(Collectors.groupingBy(IEntityField::name));
        codedFields_related = allFields_related.stream().collect(Collectors.groupingBy(IEntityField::name));
    }

    public Optional<IEntityField> field(long id){
        return field(id, ALL);
    }

    public Optional<IEntityField> field(long id, FieldScope scope){

        if(scope == null){
            scope = ALL;
        }

        Optional<IEntityField> retFieldOp = Optional.empty();

        switch(scope){
            case SELF_ONLY:
                retFieldOp = Optional.ofNullable(idMappingFields_self.get(id)).map(x -> x.get(0));
                break;
            case RELATED_ONLY:
                retFieldOp = Optional.ofNullable(idMappingFields_related.get(id)).map(x -> x.get(0));
                break;
            case ALL:
                retFieldOp = OptionalHelper.combine(
                          Optional.ofNullable(idMappingFields_self.get(id)).map(x -> x.get(0))
                        , Optional.ofNullable(idMappingFields_related.get(id)).map(x -> x.get(0)));
                break;
            default:
        }
        return retFieldOp;
    }

    private boolean checkAmbiguous(List<IEntityField> candidates){
        return candidates.stream().distinct().count() > 1;
    }

    public Optional<IEntityField> field(String code, FieldScope scope){

        if(scope == null){
            scope = ALL;
        }

        List<IEntityField> codeSelectedField = Collections.emptyList();

        switch(scope){
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

        if(!codeSelectedField.isEmpty() && checkAmbiguous(codeSelectedField)) {
            logger.error(FIELD_CODE_AMBIGUOUS, code, codeSelectedField.get(0));
        }

        return codeSelectedField.isEmpty() ? Optional.empty() : Optional.ofNullable(codeSelectedField.get(0));
    }

    /**
     * only get the first
     * @param fieldCode
     * @return
     */
    public Optional<IEntityField> field(String fieldCode){
        return field(fieldCode, ALL);
    }

    /**
     * unmodifiableList
     * @return
     */
    public List<IEntityField> fields(){
        return fields(ALL);
    }

    public List<IEntityField> fields(FieldScope fieldScope){

        if(fieldScope == null){
            fieldScope = ALL;
        }

        List<IEntityField> fields = Collections.emptyList();

        switch(fieldScope){
            case SELF_ONLY:
                fields = allFields_self;
                break;
            case RELATED_ONLY:
                fields = allFields_related;
                break;
            case ALL:
                fields = new LinkedList<>();
                fields.addAll(allFields_self);
                fields.addAll(allFields_related);
                break;
            default:
        }

        return Collections.unmodifiableList(fields);
    }

    public Set<String> testBody(Map<String, Object> map){
        Set<String> inputKeys = map.keySet();
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(codedFields_self.keySet());
        allKeys.addAll(codedFields_related.keySet());
        return inputKeys.stream().filter(x -> !allKeys.contains(x)).collect(Collectors.toSet());
    }
}
