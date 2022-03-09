package com.xforceplus.ultraman.oqsengine.cdc.testhelp.repo;

import com.xforceplus.ultraman.oqsengine.cdc.testhelp.meta.EntityClassBuilder;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.cases.DynamicCanalEntryCase;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.cases.StaticCanalEntryCase;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.meta.EntityFieldRepo;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant;
import io.vavr.Tuple2;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class StaticCanalEntryRepo {

    public static Tuple2<DynamicCanalEntryCase, StaticCanalEntryCase> CASE_STATIC_ALL_IN_ONE =
                    new Tuple2<>(
                        DynamicCanalEntryCase.anCase()
                            .withId(1001)
                            .withDeleted(false)
                            .withReplacement(true)
                            .withVersion(1)
                            .withQqsmajor(0)
                            .withCreate(System.currentTimeMillis())
                            .withUpdate(System.currentTimeMillis())
                            .withTx(100)
                            .withCommitId(100)
                            .withLevelOrdinal(1)
                            .withEntityId(EntityClassBuilder.ENTITY_CLASS_STATIC.id())
                            .withProfile(""),
                        StaticCanalEntryCase.anCase()
                            .withId(EntityFieldRepo.ID_FIELD, 1001)
                            .withBoolCol(EntityFieldRepo.BOOL_FIELD, true)
                            .withDateTimeCol(EntityFieldRepo.DATE_TIME_FIELD, "2022-01-05T00:16:10")
                            .withDecimalCol(EntityFieldRepo.DECIMAL_FIELD, "123.00")
                            .withEnumCol(EntityFieldRepo.ENUM_FIELD, "TestEnum1")
                            .withLongCol(EntityFieldRepo.LONG_FIELD, 2201102)
                            .withStringCol(EntityFieldRepo.STRING_FIELD, "TestString1")
                    );

    public static Tuple2<DynamicCanalEntryCase, StaticCanalEntryCase> CASE_STATIC_MAINTAIN =
                    new Tuple2<>(
                        DynamicCanalEntryCase.anCase()
                            .withId(1002)
                            .withDeleted(false)
                            .withReplacement(true)
                            .withVersion(1)
                            .withQqsmajor(0)
                            .withCreate(System.currentTimeMillis())
                            .withUpdate(System.currentTimeMillis())
                            .withTx(200)
                            .withCommitId(CDCConstant.MAINTAIN_COMMIT_ID)
                            .withLevelOrdinal(1)
                            .withEntityId(EntityClassBuilder.ENTITY_CLASS_STATIC.id())
                            .withProfile(""),
                        StaticCanalEntryCase.anCase()
                            .withId(EntityFieldRepo.ID_FIELD,1002)
                            .withBoolCol(EntityFieldRepo.BOOL_FIELD,true)
                            .withDateTimeCol(EntityFieldRepo.DATE_TIME_FIELD,"2022-02-04T00:16:10")
                            .withDecimalCol(EntityFieldRepo.DECIMAL_FIELD,"21213.00")
                            .withEnumCol(EntityFieldRepo.ENUM_FIELD,"TestEnum2")
                            .withLongCol(EntityFieldRepo.LONG_FIELD,3201103)
                            .withStringCol(EntityFieldRepo.STRING_FIELD,"TestString2")
        );
}
