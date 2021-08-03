package com.xforceplus.ultraman.oqsengine.calculation.lookup.helper;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * lookup 帮助工具.
 *
 * @author dongbin
 * @version 0.1 2021/08/02 18:07
 * @since 1.8
 */
public class LookupHelper {

    private static final String LINK_KEY_PREFIX = "l";

    /**
     * 构造lookup的link信息记录KEY.<br>
     * KEY的组成方式如下.<br>
     * l-目标字段id-目标实例id-发起lookup实例id 如下.<br>
     * l-912393213123-123901923232-12312312312321
     *
     * @param targetEntity 目标实例.
     * @param targetField 目标字段.
     * @param lookupEntity 发起lookup的实例.
     * @return link 的 key.
     */
    public static String buildLookupLinkKey(IEntity targetEntity, IEntityField targetField, IEntity lookupEntity) {
        StringBuilder buff = new StringBuilder();
        buff.append(LINK_KEY_PREFIX)
            .append('-')
            .append(targetField.id())
            .append('-')
            .append(targetEntity.id())
            .append('-')
            .append(lookupEntity.id());
        return buff.toString();
    }

    /**
     * 构造迭代指定实例字段迭代前辍key.
     * key的组成方式如下.<br>
     * l-目标字段id-目标实例id
     *
     * @param targetEntity 目标实例.
     * @param targetField 目标字段.
     * @return link 的 key 前辍.
     */
    public static String buildIteratorPrefixLinkKey(IEntity targetEntity, IEntityField targetField) {
        StringBuilder buff = new StringBuilder();
        buff.append(LINK_KEY_PREFIX)
            .append('-')
            .append(targetField.id())
            .append('-')
            .append(targetEntity.id());
        return buff.toString();
    }
}
