package com.xforceplus.ultraman.oqsengine.cdc.testhelp;


import com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class DynamicCanalEntryRepo {

    public static CanalEntryCase CASE_NORMAL_0 = CanalEntryCase.anCase()
        .withId(1)
        .withLevelOrdinal(1)
        .withDeleted(false)
        .withReplacement(false)
        .withVersion(1)
        .withQqsmajor(0)
        .withCreate(System.currentTimeMillis())
        .withUpdate(System.currentTimeMillis())
        .withTx(1)
        .withCommitId(1)
        .withAttr(AttributeRepo.attrs[0])
        .withEntityId(EntityClassBuilder.ENTITY_CLASS_0.id())
        .withProfile("");


    public static CanalEntryCase CASE_NORMAL_1 = CanalEntryCase.anCase()
        .withId(2)
        .withLevelOrdinal(2)
        .withDeleted(false)
        .withReplacement(false)
        .withVersion(1)
        .withQqsmajor(0)
        .withCreate(System.currentTimeMillis())
        .withUpdate(System.currentTimeMillis())
        .withTx(1)
        .withCommitId(1)
        .withAttr(AttributeRepo.attrs[1])
        .withEntityId(EntityClassBuilder.ENTITY_CLASS_1.id())
        .withProfile("");


    public static CanalEntryCase CASE_NORMAL_2 = CanalEntryCase.anCase()
        .withId(3)
        .withLevelOrdinal(3)
        .withDeleted(false)
        .withReplacement(false)
        .withVersion(1)
        .withQqsmajor(0)
        .withCreate(System.currentTimeMillis())
        .withUpdate(System.currentTimeMillis())
        .withTx(1)
        .withCommitId(1)
        .withAttr(AttributeRepo.attrs[2])
        .withEntityId(EntityClassBuilder.ENTITY_CLASS_2.id())
        .withProfile("");

    public static CanalEntryCase CASE_MAINTAIN = CanalEntryCase.anCase()
        .withId(Long.MAX_VALUE - 1)
        .withLevelOrdinal(3)
        .withDeleted(false)
        .withReplacement(false)
        .withVersion(1)
        .withQqsmajor(0)
        .withCreate(System.currentTimeMillis())
        .withUpdate(System.currentTimeMillis())
        .withTx(1)
        .withCommitId(CDCConstant.MAINTAIN_COMMIT_ID)
        .withAttr(AttributeRepo.attrs[2])
        .withEntityId(EntityClassBuilder.ENTITY_CLASS_2.id())
        .withProfile("");



    /**
     * 准备数据.
     */
    public static class AttributeRepo {
        public static String[] attrs = {
            "{\"1L\":73550,\"2S\":\"1\",\"3L\":0}",
            "{\"1L\":55304234,\"2S\":\"2222\",\"3L\":1,\"4L\":12342354353412,\"5S\":\"1.2\"}",
            "{\"1L\":55304234,\"2S\":\"2222\",\"3L\":1,\"4L\":12342354353412,\"5S\":\"1.2\",\"6S\":\"ENUM\",\"7S\":\"[1][2][3][500002][测试]\"}"
        };

        public static String[] attrsErrors = {
            "{\"1L\":73550,\"2S\":\"1'\",\"3L\":0}",
            "{\"1L\":55304234,\"2S\":\"22'22\",\"3L\":1,\"4L\":12342354353412,\"5S\":\"1.2\"}",
            "{\"1L\":55304234,\"2S\":\"2222\",\"3L\":1,\"4L\":12342354353412,\"5S\":\"1.2\",\"6S\":\"E'NUM\",\"7S\":\"[1][2][3][500002][测试]\"}"
        };
    }
}
