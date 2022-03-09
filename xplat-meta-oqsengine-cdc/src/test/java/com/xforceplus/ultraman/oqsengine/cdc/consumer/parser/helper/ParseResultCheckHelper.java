package com.xforceplus.ultraman.oqsengine.cdc.consumer.parser.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.ColumnsUtils;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.cases.DynamicCanalEntryCase;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.cases.StaticCanalEntryCase;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.meta.EntityFieldRepo;
import com.xforceplus.ultraman.oqsengine.common.serializable.utils.JacksonDefaultMapper;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.utils.DevOpsUtils;
import com.xforceplus.ultraman.oqsengine.pojo.define.OperationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import io.vavr.Tuple2;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class ParseResultCheckHelper {

    public static void dynamicCheck(DynamicCanalEntryCase expected, OriginalEntity actual) throws JsonProcessingException {
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.isDeleted(), actual.isDeleted());
        Assertions.assertEquals(expected.isDeleted() ? OperationType.DELETE.getValue() : OperationType.UPDATE.getValue(), actual.getOp());
        Assertions.assertEquals(expected.getVersion(), actual.getVersion());
        Assertions.assertEquals(expected.getOqsmajor(), actual.getOqsMajor());
        Assertions.assertEquals(expected.getCreate(), actual.getCreateTime());
        Assertions.assertEquals(expected.getUpdate(), actual.getUpdateTime());
        Assertions.assertEquals(expected.getTx(), actual.getTx());
        Assertions.assertEquals(expected.getCommitId(), actual.getCommitid());
        Assertions.assertEquals(expected.getEntityId(), actual.getEntityClass().id());
        Assertions.assertEquals(expected.getAttr(), JacksonDefaultMapper.OBJECT_MAPPER.writeValueAsString(actual.getAttributes()));

        if (DevOpsUtils.isMaintainRecord(expected.getCommitId())) {
            Assertions.assertEquals(expected.getTx(), actual.getMaintainid());
        }
    }


    public static void staticCheck(Tuple2<DynamicCanalEntryCase, StaticCanalEntryCase> expected, OriginalEntity actual, boolean checkStaticAttr) {
        Assertions.assertEquals(expected._1().getId(), actual.getId());
        Assertions.assertEquals(expected._1().getCommitId(), actual.getCommitid());
        Assertions.assertEquals(expected._1().getEntityId(), actual.getEntityClass().id());
        Assertions.assertEquals(expected._1().getOp(), actual.getOp());
        Assertions.assertEquals(expected._1().getEntityId(), actual.getEntityClassRef().getId());
        Assertions.assertEquals(expected._1().getProfile(), actual.getEntityClassRef().getProfile());
        Assertions.assertEquals(expected._1().getTx(), actual.getTx());
        Assertions.assertEquals(expected._1().isDeleted(), actual.isDeleted());
        Assertions.assertEquals(expected._1().getVersion(), actual.getVersion());
        Assertions.assertEquals(expected._1().getCreate(), actual.getCreateTime());
        Assertions.assertEquals(expected._1().getUpdate(), actual.getUpdateTime());
        Assertions.assertEquals(expected._1().getOqsmajor(), actual.getOqsMajor());
        if (DevOpsUtils.isMaintainRecord(expected._1().getCommitId())) {
            Assertions.assertEquals(expected._1().getTx(), actual.getMaintainid());
        }
        if (checkStaticAttr) {
            //  去掉id
            Assertions.assertEquals(expected._2().getContext().size() - 1, actual.attributeSize());
            for (Map.Entry<String, Tuple2<IEntityField, Object>> entry : expected._2().getContext().entrySet()) {
                if (!entry.getValue()._1().name().equals(EntityFieldRepo.ID_FIELD.name())) {
                    check(entry.getValue()._2(), actual.getAttributes().get(toStorageKey(entry.getValue()._1())),
                        entry.getValue()._1().type());
                }
            }
        }
    }

    public static void check(Object expected, Object actual, FieldType f) {
        switch (f) {
            case DATETIME:
                Assertions.assertEquals(ColumnsUtils.toEpochMilli((String) expected), actual);
                break;
            case BOOLEAN:
                Assertions.assertEquals((boolean) expected ? 1L : 0L, Long.parseLong(actual.toString()));
                break;
            case LONG:
                Assertions.assertEquals(Long.parseLong(expected.toString()), Long.parseLong(actual.toString()));
            default: {
                Assertions.assertEquals(expected, actual);
                break;
            }
        }
    }

    public static String toStorageKey(IEntityField field) {
        return "F" + field.id() + toStorageSuffix(field.type());
    }

    private static String toStorageSuffix(FieldType fieldType) {
        switch (fieldType) {
            case BOOLEAN:
            case DATETIME:
            case LONG:
                return "L";
            default:
                return "S";
        }
    }
}
