package com.xforceplus.ultraman.oqsengine.calculation.helper;

import com.xforceplus.ultraman.oqsengine.common.NumberUtils;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * lookup 帮助工具.
 *
 * @author dongbin
 * @version 0.1 2021/08/02 18:07
 * @since 1.8
 */
public class LookupHelper {

    /**
     * 前辍.
     */
    public static final String LINK_KEY_PREFIX = "lookup";

    /**
     * lookup 对象的元信息标识前辍.
     */
    public static final String LINK_KEY_LOOKUP_ENTITYCLASS_PREFIX = "l";
    /**
     * 目标字段id标识前辍.
     */
    public static final String LINK_KEY_TARGET_FIELD_PREFIX = "f";
    /**
     * lookup对象的实例前辍.
     */
    public static final String LINK_KEY_LOOKUP_ENTITY_PREFIX = "e";

    /**
     * 构造lookup的link信息记录KEY.<br>
     * KEY的组成方式如下.<br>
     * l-发起lookup的entityClassid-目标字段id-发起lookup实例id 如下.<br>
     * l-912393213123-123901923232-1231231231232
     *
     * @param targetField  目标字段.
     * @param lookupEntity 发起lookup的实例.
     * @return link 的 key.
     */
    public static String buildLookupLinkKey(IEntityField targetField, IEntity lookupEntity) {
        StringBuilder buff = new StringBuilder();
        buff.append(LINK_KEY_PREFIX)
            .append('-')
            .append(LINK_KEY_LOOKUP_ENTITYCLASS_PREFIX)
            .append(NumberUtils.zeroFill(lookupEntity.entityClassRef().getId()))
            .append('-')
            .append(LINK_KEY_TARGET_FIELD_PREFIX)
            .append(NumberUtils.zeroFill(targetField.id()))
            .append('-')
            .append(LINK_KEY_LOOKUP_ENTITY_PREFIX)
            .append(NumberUtils.zeroFill(lookupEntity.id()));

        return buff.toString();
    }

    /**
     * 构造迭代指定实例字段迭代前辍key.
     * 可选只包含指定lookup类型的.<br>
     * key的组成方式如下.<br>
     * l-发起lookup的entityClassid-目标字段id
     *
     * @param targetField       目标字段.
     * @param lookupEntityClass 发起lookup的元信息.
     * @return link 的 key 前辍.
     */
    public static String buildIteratorPrefixLinkKey(IEntityField targetField, IEntityClass lookupEntityClass) {
        StringBuilder buff = new StringBuilder();
        buff.append(LINK_KEY_PREFIX)
            .append('-')
            .append(LINK_KEY_LOOKUP_ENTITYCLASS_PREFIX)
            .append(NumberUtils.zeroFill(lookupEntityClass.id()))
            .append('-')
            .append(LINK_KEY_TARGET_FIELD_PREFIX)
            .append(NumberUtils.zeroFill(targetField.id()));

        return buff.toString();
    }
}
