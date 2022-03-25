package com.xforceplus.ultraman.oqsengine.storage.master.utils;

import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    /**
     * 根据对象查找大量元信息,会尽量保证少的调用MetaManager造成性能压力.
     * 因为MetaManager的实现在每一次调用时都会命中一次redis,这将造成过多的IO.
     * 如果某个元信息指针失效,那么将返回一个空的列表.
     *
     * @param entities    需要查找元信息的对象.
     * @param metaManager 元数据管理器.
     * @return 元信息
     */
    public static final IEntityClass[] findLargeEntityClass(IEntity[] entities, MetaManager metaManager) {
        EntityClassRef[] refs = Arrays.stream(entities).map(e -> e.entityClassRef()).toArray(EntityClassRef[]::new);
        return findLargeEntityClsss(refs, metaManager);
    }

    /**
     * 查找大量EntityClass信息.会尽景保证少对于MetaManager的调用.
     * 因为MetaManager的实现在每一次调用时都会命中一次redis,这将造成过多的IO.
     * 如果某个元信息指针失效,那么将返回一个空的列表.
     *
     * @param entityClassRefs entityClass指针信息.
     * @param metaManager     元数据管理器.
     * @return 元信息.
     */
    public static final IEntityClass[] findLargeEntityClsss(EntityClassRef[] entityClassRefs, MetaManager metaManager) {
        Map<EntityClassRef, IEntityClass> entityClassCache = new HashMap<>();
        EntityClassRef entityClassRef;
        IEntityClass entityClass;
        Optional<IEntityClass> entityClassOp;
        IEntityClass[] entityClasses = new IEntityClass[entityClassRefs.length];
        for (int i = 0; i < entityClassRefs.length; i++) {
            entityClassRef = entityClassRefs[i];

            entityClass = entityClassCache.get(entityClassRef);

            if (entityClass != null) {
                entityClasses[i] = entityClass;
            } else {
                // 缓存中没有找到.
                entityClassOp = metaManager.load(entityClassRef);
                if (entityClassOp.isPresent()) {
                    entityClass = entityClassOp.get();
                    entityClassCache.put(entityClassRef, entityClass);
                    entityClasses[i] = entityClass;
                } else {
                    return new IEntityClass[0];
                }
            }
        }

        return entityClasses;
    }
}
