package com.xforceplus.ultraman.oqsengine.sdk.service.export.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityService;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityServiceEx;
import com.xforceplus.ultraman.oqsengine.sdk.service.export.ExportCustomFieldToString;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.DictItem;
import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default Export to String
 */
public class DefaultExportCustomFieldToString implements ExportCustomFieldToString {

    @Autowired
    private EntityServiceEx entityServiceEx;

    @Autowired
    private EntityService entityService;

    private Logger logger = LoggerFactory.getLogger(DefaultExportCustomFieldToString.class);

    @Override
    public boolean isSupport(IEntityClass entityClass, IEntityField field) {
        return true;
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public Optional<String> getString(IEntityClass entityClass, IEntityField entityField, String safeSourceValue, Map<String, Object> context) {

        String retStr = null;

        FieldType type = entityField.type();

        /**
         * dict cache
         */
        Map<String, List<DictItem>> mapping = (Map<String, List<DictItem>>) context.getOrDefault("dictCache", new HashMap<>());
        context.putIfAbsent("dictCache", mapping);

        /**
         * related entity
         */
        Map<String, Map<String, String>> searchTable = (Map<String, Map<String, String>>) context.getOrDefault("search", new HashMap<>());
        context.putIfAbsent("search", mapping);

        switch (type) {
            case ENUM:
                String dictId = entityField.dictId();
                List<DictItem> items = mapping.get(dictId);
                if (items == null) {
                    List<DictItem> dictItems = entityServiceEx.findDictItems(dictId, null);
                    mapping.put(dictId, dictItems);
                    items = dictItems;
                }

                retStr = items.stream()
                        .filter(x -> x.getValue().equals(safeSourceValue))
                        .map(DictItem::getText)
                        .findAny().orElse("");
                break;
            case DATETIME:
                retStr = entityField.type().toTypedValue(entityField, safeSourceValue).map(x -> {
                    return ((DateTimeValue) x);
                }).map(x -> x.getValue().format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:SS"))).orElse("");
                break;
            case STRINGS:

                if (safeSourceValue.trim().isEmpty()) {
                    retStr = "";
                    break;
                }

                String[] ids = safeSourceValue.split(",");
                Long fieldId = entityField.id();

                Optional<Long> relatedId = entityClass.relations().stream()
                        .filter(x -> x.getEntityField() != null)
                        .filter(x -> x.getEntityField().id() == fieldId)
                        .map(Relation::getEntityClassId).findAny();

                if (relatedId.isPresent()) {
                    Optional<IEntityClass> relatedEntityOp = entityService.load(relatedId.get().toString());
                    if (relatedEntityOp.isPresent()) {
                        IEntityClass relatedEntity = relatedEntityOp.get();

                        Map<String, String> cachedList = searchTable.get(relatedId.get().toString());
                        if (cachedList == null) {
                            //search
                            Map<String, String> cached = new HashMap<>();
                            searchTable.put(relatedId.get().toString(), cached);
                            cachedList = cached;
                        }

                        //TODO how to find all
                        Map<String, String> finalCachedList = cachedList;


                        //try to find something to display
                        Optional<IEntityField> displayField = relatedEntity.fields().stream()
                                .filter(x -> Optional.ofNullable(x.config())
                                        .filter(field -> "1".equals(field.getDisplayType())).isPresent()).findFirst();

                        retStr = Stream.of(ids).map(x -> {
                            String name = finalCachedList.get(x);
                            String innerRetStr = x;
                            if (name == null) {
                                //only when displayField is present will find one for id
                                if (displayField.isPresent()) {
                                    try {
                                        //search
                                        Either<String, Map<String, Object>> one = entityService.findOne(relatedEntity, Long.parseLong(x));
                                        if (one.isRight()) {
                                            String key = displayField.get().name();
                                            Object relatedDisplayName = one.get().get(key);
                                            if (relatedDisplayName != null) {
                                                innerRetStr = relatedDisplayName.toString();
                                            }
                                        }
                                    } catch (Exception ex) {
                                        logger.error("{}", ex);
                                    }
                                }
                                finalCachedList.put(x, innerRetStr);
                            } else {
                                innerRetStr = name;
                            }

                            return innerRetStr;
                        }).collect(Collectors.joining(","));
                    }
                }
                break;

            default:
                retStr = entityField.type().toTypedValue(entityField, safeSourceValue)
                        .map(IValue::getValue)
                        .map(Object::toString).orElse("");

        }

        return Optional.ofNullable(retStr);
    }
}
