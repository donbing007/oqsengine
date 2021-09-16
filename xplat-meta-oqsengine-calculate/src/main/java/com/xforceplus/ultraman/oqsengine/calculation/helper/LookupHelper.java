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
     * key的组装分割符.
     */
    public static final String LINKE_KEY_SPACE = "-";

    /**
     * 前辍.
     */
    public static final String LINK_KEY_PREFIX = "lookup";

    /**
     * 目标字段id标识前辍.
     */
    public static final String LINK_KEY_TARGET_FIELD_PREFIX = "tf";
    /**
     * 目标实例id标识.
     */
    public static final String LINK_KEY_TARGET_ENTITY_PREFIX = "te";
    /**
     * lookup 对象的元信息标识前辍.
     */
    public static final String LINK_KEY_LOOKUP_ENTITYCLASS_PREFIX = "lc";

    /**
     * entityclass 对象的元信息profile前缀.
     */
    public static final String LINK_KEY_LOOKUP_PROFILE_PREFIX = "lp";

    /**
     * lookup对象的实例前辍.
     */
    public static final String LINK_KEY_LOOKUP_ENTITY_PREFIX = "le";
    /**
     * lookup的字段标识.
     */
    public static final String LINK_KEY_LOOKUP_FIELD_PREFIX = "lf";


    /**
     * 构造lookup的link信息记录KEY.<br>
     * KEY的组成方式如下.<br>
     * 共同前辍-{标识}目标字段id-{标识}目标实例id-{标识}发起lookup的entityClassid-{标识}发起lookup的字段标识-{标识}发起lookup实例id.
     *
     * @param targetEntity 目标实例.
     * @param targetField  目标字段.
     * @param lookupEntity 发起lookup的实例.
     * @param lookupField  发起lookup的字段.
     * @return link 的 key.
     */
    public static LookupLinkKey buildLookupLinkKey(
        IEntity targetEntity, IEntityField targetField, IEntity lookupEntity, IEntityField lookupField) {

        return new LookupLinkKey(
            targetField.id(),
            targetEntity.id(),
            lookupEntity.entityClassRef().getId(),
            lookupEntity.entityClassRef().getProfile(),
            lookupField.id(),
            lookupEntity.id()
        );
    }

    /**
     * 解析 link key.
     *
     * @param stringKey 目标key.
     * @return 解析结果.
     */
    public static LookupLinkKey parseLinkKey(String stringKey) {
        // key 应该按分割符分割后长度为6.
        final int keyLen = 7;
        String[] keys = stringKey.split(LINKE_KEY_SPACE);
        if (keys.length != keyLen) {
            throw new IllegalArgumentException(String.format("Incorrect lookup connection key.[%s]", stringKey));
        }

        long targetFieldId = 0;
        long targetEntityId = 0;
        long lookupClassId = 0;
        long lookupFieldId = 0;
        long lookupEntityId = 0;
        String lookupProfile = "";
        /*
        忽略共同前辍
         */
        String keyPeriod;
        for (int i = 1; i < keyLen; i++) {
            keyPeriod = keys[i];
            if (keyPeriod.startsWith(LINK_KEY_TARGET_FIELD_PREFIX)) {

                targetFieldId = Long.parseLong(keyPeriod.substring(LINK_KEY_TARGET_FIELD_PREFIX.length()));

            } else if (keyPeriod.startsWith(LINK_KEY_TARGET_ENTITY_PREFIX)) {

                targetEntityId = Long.parseLong(keyPeriod.substring(LINK_KEY_TARGET_ENTITY_PREFIX.length()));

            } else if (keyPeriod.startsWith(LINK_KEY_LOOKUP_ENTITYCLASS_PREFIX)) {

                lookupClassId = Long.parseLong(keyPeriod.substring(LINK_KEY_LOOKUP_ENTITYCLASS_PREFIX.length()));

            } else if (keyPeriod.startsWith(LINK_KEY_LOOKUP_FIELD_PREFIX)) {

                lookupFieldId = Long.parseLong(keyPeriod.substring(LINK_KEY_LOOKUP_FIELD_PREFIX.length()));

            } else if (keyPeriod.startsWith(LINK_KEY_LOOKUP_ENTITY_PREFIX)) {

                lookupEntityId = Long.parseLong(keyPeriod.substring(LINK_KEY_LOOKUP_ENTITY_PREFIX.length()));

            } else if (keyPeriod.startsWith(LINK_KEY_LOOKUP_PROFILE_PREFIX)) {

                lookupProfile = keyPeriod.substring(LINK_KEY_LOOKUP_PROFILE_PREFIX.length());

            } else {
                throw new IllegalArgumentException(String.format("Incorrect lookup connection key.[%s]", stringKey));
            }
        }

        return new LookupLinkKey(targetFieldId, targetEntityId, lookupClassId, lookupProfile, lookupFieldId,
            lookupEntityId);
    }

    /**
     * 构造迭代指定实例字段迭代前辍key.
     * 可选只包含指定lookup类型的.<br>
     * key的组成方式如下.<br>
     * 共同前辍-{标识}目标字段id-{标识}目标实例id-{标识}发起lookup的entityClassid-{标识}发起lookup的字段标识
     *
     * @param targetField       目标字段.
     * @param lookupEntityClass 发起lookup的元信息.
     * @return link 的 key 前辍.
     */
    public static LookupLinkIterKey buildIteratorPrefixLinkKey(
        IEntity targetEntity, IEntityField targetField, IEntityClass lookupEntityClass, IEntityField lookupField) {

        return new LookupLinkIterKey(targetField.id(),
            targetEntity.id(), lookupEntityClass.id(),
            lookupField.id());
    }

    /**
     * 迭代key.
     */
    public static class LookupLinkIterKey {
        private long targetFieldId;
        private long targetEntityId;
        private long lookupClassId;
        private long lookupFieldId;

        /**
         * 构造迭代key.
         *
         * @param targetFieldId  目标字段id.
         * @param targetEntityId 目标实例id.
         * @param lookupClassId  lookup类型id.
         * @param lookupFieldId  lookup字段
         */
        public LookupLinkIterKey(long targetFieldId, long targetEntityId,
                                 long lookupClassId, long lookupFieldId) {
            this.targetFieldId = targetFieldId;
            this.targetEntityId = targetEntityId;
            this.lookupClassId = lookupClassId;
            this.lookupFieldId = lookupFieldId;
        }

        public long getTargetFieldId() {
            return targetFieldId;
        }

        public long getTargetEntityId() {
            return targetEntityId;
        }

        public long getLookupClassId() {
            return lookupClassId;
        }

        public long getLookupFieldId() {
            return lookupFieldId;
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer();
            sb.append(LINK_KEY_PREFIX)
                .append(LINKE_KEY_SPACE)
                .append(LINK_KEY_TARGET_FIELD_PREFIX)
                .append(NumberUtils.zeroFill(targetFieldId))
                .append(LINKE_KEY_SPACE)
                .append(LINK_KEY_TARGET_ENTITY_PREFIX)
                .append(NumberUtils.zeroFill(targetEntityId))
                .append(LINKE_KEY_SPACE)
                .append(LINK_KEY_LOOKUP_ENTITYCLASS_PREFIX)
                .append(NumberUtils.zeroFill(lookupClassId))
                .append(LINKE_KEY_SPACE)
                .append(LINK_KEY_LOOKUP_FIELD_PREFIX)
                .append(NumberUtils.zeroFill(lookupFieldId));

            return sb.toString();
        }
    }

    /**
     * lookup 字段的连接key.
     */
    public static class LookupLinkKey {
        private long targetFieldId;
        private long targetEntityId;
        private long lookupClassId;
        private long lookupFieldId;
        private long lookupEntityId;
        private String lookupProfile;

        /**
         * 实例.
         *
         * @param targetFieldId  目标字段id.
         * @param targetEntityId 目标实例id.
         * @param lookupClassId  lookup类型id.
         * @param lookupFieldId  lookup字段id.
         * @param lookupEntityId lookup实例
         */
        public LookupLinkKey(long targetFieldId,
                             long targetEntityId,
                             long lookupClassId,
                             String lookupProfile,
                             long lookupFieldId,
                             long lookupEntityId) {
            this.targetFieldId = targetFieldId;
            this.targetEntityId = targetEntityId;
            this.lookupClassId = lookupClassId;
            this.lookupProfile = lookupProfile;
            this.lookupFieldId = lookupFieldId;
            this.lookupEntityId = lookupEntityId;
        }

        public long getTargetFieldId() {
            return targetFieldId;
        }

        public long getTargetEntityId() {
            return targetEntityId;
        }

        public long getLookupClassId() {
            return lookupClassId;
        }

        public long getLookupFieldId() {
            return lookupFieldId;
        }

        public long getLookupEntityId() {
            return lookupEntityId;
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer();

            sb.append(LINK_KEY_PREFIX)
                .append(LINKE_KEY_SPACE)
                .append(LINK_KEY_TARGET_FIELD_PREFIX)
                .append(NumberUtils.zeroFill(targetFieldId))
                .append(LINKE_KEY_SPACE)
                .append(LINK_KEY_TARGET_ENTITY_PREFIX)
                .append(NumberUtils.zeroFill(targetEntityId))
                .append(LINKE_KEY_SPACE)
                .append(LINK_KEY_LOOKUP_ENTITYCLASS_PREFIX)
                .append(NumberUtils.zeroFill(lookupClassId))
                .append(LINKE_KEY_SPACE)
                .append(LINK_KEY_LOOKUP_FIELD_PREFIX)
                .append(NumberUtils.zeroFill(lookupFieldId))
                .append(LINKE_KEY_SPACE)
                .append(LINK_KEY_LOOKUP_ENTITY_PREFIX)
                .append(NumberUtils.zeroFill(lookupEntityId))
                .append(LINKE_KEY_SPACE)
                .append(LINK_KEY_LOOKUP_PROFILE_PREFIX)
                .append(lookupProfile);

            return sb.toString();
        }
    }
}
