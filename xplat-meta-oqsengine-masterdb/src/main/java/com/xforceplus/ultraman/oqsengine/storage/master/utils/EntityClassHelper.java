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

    /**
     * 构造类系统查询SQL.
     *
     * @param entityClass 目标entityClass.
     * @return 查询SQL.
     */
    public static String buildEntityClassQuerySql(IEntityClass entityClass) {
        StringBuilder buff = new StringBuilder();
        buff.append("(");
        int emptyLen = buff.length();
        entityClass.family().stream().forEach(es -> {
            if (buff.length() > emptyLen) {
                buff.append(" AND ");
            }
            buff.append(ENTITY_COLUMNS[es.level()]).append(" = ").append(es.id());
        });

        buff.append(")");
        return buff.toString();
    }
}
