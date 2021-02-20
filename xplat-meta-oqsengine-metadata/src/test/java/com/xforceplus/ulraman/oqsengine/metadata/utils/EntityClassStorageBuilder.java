package com.xforceplus.ulraman.oqsengine.metadata.utils;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.oqs.OqsRelation;

/**
 * desc :
 * name : EntityClassStorageBuilder
 *
 * @author : xujia
 * date : 2021/2/16
 * @since : 1.8
 */
public class EntityClassStorageBuilder {

    public static OqsRelation relationLong(long id, long fieldId) {
        return new OqsRelation("test" + id, id, "order", false, fieldId);
    }

    public static OqsRelation relationString(long id, long fieldId) {
        return new OqsRelation("test" + id, id, "order", false, fieldId);
    }

    public static IEntityField entityFieldLong(long id) {
        return new EntityField(id, "id" + id, FieldType.LONG, FieldConfig.build().searchable(true).identifie(false));
    }

    public static IEntityField entityFieldString(long id) {
        return new EntityField(id, "id" + id, FieldType.STRING, FieldConfig.build().searchable(true).identifie(false));
    }
}
