package com.xforceplus.ultraman.oqsengine.calculation.helper;

import com.xforceplus.ultraman.oqsengine.common.NumberUtils;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.Optional;

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
    private static final String LINK_KEY_PREFIX = "l";

    /**
     * 构造lookup的link信息记录KEY.<br>
     * KEY的组成方式如下.<br>
     * l-目标字段id-目标实例id-发起lookup的entityClassid-发起lookup实例id 如下.<br>
     * l-912393213123-123901923232-1231231231232-12312312312321
     *
     * @param targetEntity 目标实例.
     * @param targetField  目标字段.
     * @param lookupEntity 发起lookup的实例.
     * @return link 的 key.
     */
    public static String buildLookupLinkKey(IEntity targetEntity, IEntityField targetField, IEntity lookupEntity) {
        StringBuilder buff = new StringBuilder();
        buff.append(LINK_KEY_PREFIX)
            .append('-')
            .append(NumberUtils.zeroFill(targetField.id()))
            .append('-')
            .append(NumberUtils.zeroFill(targetEntity.id()))
            .append('-')
            .append(NumberUtils.zeroFill(lookupEntity.entityClassRef().getId()))
            .append('-')
            .append(NumberUtils.zeroFill(lookupEntity.id()));
        return buff.toString();
    }

    /**
     * 构造迭代前辍key,包含所有发起的lookup类型.
     *
     * @param targetEntity 目标实例.
     * @param targetField  目标字段.
     * @return link 的 key 前辍.
     */
    public static String buildIteratorPrefixLinkKey(IEntity targetEntity, IEntityField targetField) {
        return buildIteratorPrefixLinkKey(targetEntity, targetField, Optional.empty());
    }

    /**
     * 构造迭代指定实例字段迭代前辍key.
     * 可选只包含指定lookup类型的.<br>
     * key的组成方式如下.<br>
     * l-目标字段id-目标实例id
     *
     * @param targetEntity        目标实例.
     * @param targetField         目标字段.
     * @param lookupEntityClassOp 可选的发起lookup的类型.
     * @return link 的 key 前辍.
     */
    public static String buildIteratorPrefixLinkKey(IEntity targetEntity,
                                                    IEntityField targetField,
                                                    Optional<IEntityClass> lookupEntityClassOp) {
        StringBuilder buff = new StringBuilder();
        buff.append(LINK_KEY_PREFIX)
            .append('-')
            .append(NumberUtils.zeroFill(targetField.id()))
            .append('-')
            .append(NumberUtils.zeroFill(targetEntity.id()));

        if (lookupEntityClassOp.isPresent()) {
            buff.append('-')
                .append(NumberUtils.zeroFill(lookupEntityClassOp.get().id()));
        }
        return buff.toString();
    }
}
