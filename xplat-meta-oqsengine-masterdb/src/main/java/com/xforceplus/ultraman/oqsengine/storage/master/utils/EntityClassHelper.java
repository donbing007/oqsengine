package com.xforceplus.ultraman.oqsengine.storage.master.utils;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;

/**
 * entityclass 帮助方法.
 *
 * @author dongbin
 * @version 0.1 2021/2/22 17:42
 * @since 1.8
 */
public class EntityClassHelper {

    private static final String[] ENTITY_COLUMNS = {
        FieldDefine.ENTITYCLASS_LEVEL_0,
        FieldDefine.ENTITYCLASS_LEVEL_1,
        FieldDefine.ENTITYCLASS_LEVEL_2,
        FieldDefine.ENTITYCLASS_LEVEL_3,
        FieldDefine.ENTITYCLASS_LEVEL_4,
    };

    // (entity0 = ? and entity1 = ? ....)
    public static String buildEntityClassQuerySql(IEntityClass entityClass) {
        StringBuilder buff = new StringBuilder();
        StringBuilder segment = new StringBuilder();
        buff.append("(");
        int emptyLen = buff.length();
        IEntityClass currentEntityClass = entityClass;
        for (int i = entityClass.level(); i >= 0; i--) {
            if (buff.length() > emptyLen) {
                buff.append(" AND ");
            }
            segment.append(ENTITY_COLUMNS[i]).append(" = ").append(currentEntityClass.id());
            buff.insert(0, segment.toString());
            segment.delete(0, segment.length());
        }
        buff.append(")");
        return buff.toString();
    }
}
