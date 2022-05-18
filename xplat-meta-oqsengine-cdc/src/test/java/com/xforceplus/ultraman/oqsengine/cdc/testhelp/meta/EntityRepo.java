package com.xforceplus.ultraman.oqsengine.cdc.testhelp.meta;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import io.vavr.Tuple2;
import java.util.Arrays;
import java.util.List;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class EntityRepo {

    public static List<Tuple2<IEntityClass, List<IEntityField>>> FULL_ENTITY_CASES = Arrays.asList(
        new Tuple2<>(EntityClassBuilder.ENTITY_CLASS_0, Arrays.asList(EntityFieldRepo.LONG_FIELD, EntityFieldRepo.STRING_FIELD)),
        new Tuple2<>(EntityClassBuilder.ENTITY_CLASS_1,
            Arrays.asList(EntityFieldRepo.LONG_FIELD, EntityFieldRepo.STRING_FIELD, EntityFieldRepo.BOOL_FIELD, EntityFieldRepo.DATE_TIME_FIELD)),
        new Tuple2<>(EntityClassBuilder.ENTITY_CLASS_2,
            Arrays.asList(EntityFieldRepo.LONG_FIELD, EntityFieldRepo.STRING_FIELD, EntityFieldRepo.BOOL_FIELD, EntityFieldRepo.DATE_TIME_FIELD,
            EntityFieldRepo.DECIMAL_FIELD, EntityFieldRepo.STRINGS_FIELD)),
        new Tuple2<>(EntityClassBuilder.ENTITY_CLASS_STATIC,
            Arrays.asList(EntityFieldRepo.LONG_FIELD, EntityFieldRepo.STRING_FIELD, EntityFieldRepo.BOOL_FIELD,
                EntityFieldRepo.DATE_TIME_FIELD, EntityFieldRepo.DECIMAL_FIELD, EntityFieldRepo.ENUM_FIELD))
    );

    public static List<Tuple2<IEntityClass, List<IEntityField>>> DYNAMIC_ENTITY_CASES = Arrays.asList(
        new Tuple2<>(EntityClassBuilder.ENTITY_CLASS_0, Arrays.asList(EntityFieldRepo.LONG_FIELD, EntityFieldRepo.STRING_FIELD)),
        new Tuple2<>(EntityClassBuilder.ENTITY_CLASS_1,
            Arrays.asList(EntityFieldRepo.LONG_FIELD, EntityFieldRepo.STRING_FIELD, EntityFieldRepo.BOOL_FIELD, EntityFieldRepo.DATE_TIME_FIELD)),
        new Tuple2<>(EntityClassBuilder.ENTITY_CLASS_2,
            Arrays.asList(EntityFieldRepo.LONG_FIELD, EntityFieldRepo.STRING_FIELD, EntityFieldRepo.BOOL_FIELD, EntityFieldRepo.DATE_TIME_FIELD,
                EntityFieldRepo.DECIMAL_FIELD, EntityFieldRepo.STRINGS_FIELD))
    );

    //  dynamic
    //  entityClass-0
    public static IEntity LONG_STRING_ENTITY_C0_1 = EntityBuilder.buildEntity(101, EntityClassBuilder.ENTITY_CLASS_0,
        Arrays.asList(EntityFieldRepo.LONG_FIELD, EntityFieldRepo.STRING_FIELD));

    public static IEntity LONG_STRING_ENTITY_C0_2 = EntityBuilder.buildEntity(102, EntityClassBuilder.ENTITY_CLASS_0,
        Arrays.asList(EntityFieldRepo.LONG_FIELD, EntityFieldRepo.STRING_FIELD));

    //  entityClass-1
    public static IEntity BOOL_DATE_ENTITY_C1_1 = EntityBuilder.buildEntity(103, EntityClassBuilder.ENTITY_CLASS_1,
        Arrays.asList(EntityFieldRepo.LONG_FIELD, EntityFieldRepo.STRING_FIELD, EntityFieldRepo.BOOL_FIELD, EntityFieldRepo.DATE_TIME_FIELD));

    public static IEntity BOOL_DATE_ENTITY_C1_2 = EntityBuilder.buildEntity(104, EntityClassBuilder.ENTITY_CLASS_1,
        Arrays.asList(EntityFieldRepo.LONG_FIELD, EntityFieldRepo.STRING_FIELD, EntityFieldRepo.BOOL_FIELD, EntityFieldRepo.DATE_TIME_FIELD));

    //  entityClass-2
    public static IEntity DECIMAL_STRINGS_ENTITY_C2_1 = EntityBuilder.buildEntity(105, EntityClassBuilder.ENTITY_CLASS_2,
        Arrays.asList(EntityFieldRepo.LONG_FIELD, EntityFieldRepo.STRING_FIELD, EntityFieldRepo.BOOL_FIELD,
            EntityFieldRepo.DATE_TIME_FIELD, EntityFieldRepo.DECIMAL_FIELD, EntityFieldRepo.STRINGS_FIELD));

    public static IEntity DECIMAL_STRINGS_ENTITY_C2_2 = EntityBuilder.buildEntity(106, EntityClassBuilder.ENTITY_CLASS_2,
        Arrays.asList(EntityFieldRepo.LONG_FIELD, EntityFieldRepo.STRING_FIELD, EntityFieldRepo.BOOL_FIELD,
            EntityFieldRepo.DATE_TIME_FIELD, EntityFieldRepo.DECIMAL_FIELD, EntityFieldRepo.STRINGS_FIELD));

    //  static
    //  entityClassStatic
    public static Tuple2<IEntity, List<Object>> STATIC_ENTITY_1 =
        new Tuple2<>(
            EntityBuilder.buildEntity(201, EntityClassBuilder.ENTITY_CLASS_STATIC,
                Arrays.asList(EntityFieldRepo.LONG_FIELD, EntityFieldRepo.STRING_FIELD, EntityFieldRepo.BOOL_FIELD,
                    EntityFieldRepo.DATE_TIME_FIELD, EntityFieldRepo.DECIMAL_FIELD, EntityFieldRepo.ENUM_FIELD)),
            Arrays.asList(1000001, "test1", true, "2022-01-01T12:00:01", 2000.01, "STW")
        );

    public static Tuple2<IEntity, List<Object>> STATIC_ENTITY_2 =
        new Tuple2<>(
            EntityBuilder.buildEntity(202, EntityClassBuilder.ENTITY_CLASS_STATIC,
                Arrays.asList(EntityFieldRepo.LONG_FIELD, EntityFieldRepo.STRING_FIELD, EntityFieldRepo.BOOL_FIELD,
                    EntityFieldRepo.DATE_TIME_FIELD, EntityFieldRepo.DECIMAL_FIELD, EntityFieldRepo.ENUM_FIELD)),
            Arrays.asList(1000002, "test2", false, "2022-01-01T12:00:21", 200001.00, "WVS")
        );

    public static Tuple2<IEntity, List<Object>> STATIC_ENTITY_3 =
        new Tuple2<>(
            EntityBuilder.buildEntity(203, EntityClassBuilder.ENTITY_CLASS_STATIC,
                Arrays.asList(EntityFieldRepo.LONG_FIELD, EntityFieldRepo.STRING_FIELD, EntityFieldRepo.BOOL_FIELD,
                    EntityFieldRepo.DATE_TIME_FIELD, EntityFieldRepo.DECIMAL_FIELD, EntityFieldRepo.ENUM_FIELD)),
            Arrays.asList(1000003, "test3", false, "2022-01-01T12:00:31", 100001.00, "STW")
        );

    public static Tuple2<IEntity, List<Object>> STATIC_ENTITY_4 =
        new Tuple2<>(
            EntityBuilder.buildEntity(204, EntityClassBuilder.ENTITY_CLASS_STATIC,
                Arrays.asList(EntityFieldRepo.LONG_FIELD, EntityFieldRepo.STRING_FIELD, EntityFieldRepo.BOOL_FIELD,
                    EntityFieldRepo.DATE_TIME_FIELD, EntityFieldRepo.DECIMAL_FIELD, EntityFieldRepo.ENUM_FIELD)),
            Arrays.asList(1000004, "test4", false, "2022-01-01T13:00:31", 100002.10, "STW")
        );

    public static Tuple2<IEntity, List<Object>> STATIC_ENTITY_5 =
        new Tuple2<>(
            EntityBuilder.buildEntity(205, EntityClassBuilder.ENTITY_CLASS_STATIC,
                Arrays.asList(EntityFieldRepo.LONG_FIELD, EntityFieldRepo.STRING_FIELD, EntityFieldRepo.BOOL_FIELD,
                    EntityFieldRepo.DATE_TIME_FIELD, EntityFieldRepo.DECIMAL_FIELD, EntityFieldRepo.ENUM_FIELD)),
            Arrays.asList(1000005, "test5", false, "2022-01-01T15:00:31", 100001.10, "OQS")
        );

    public static Tuple2<IEntity, List<Object>> STATIC_ENTITY_6 =
        new Tuple2<>(
            EntityBuilder.buildEntity(206, EntityClassBuilder.ENTITY_CLASS_STATIC,
                Arrays.asList(EntityFieldRepo.LONG_FIELD, EntityFieldRepo.STRING_FIELD, EntityFieldRepo.BOOL_FIELD,
                    EntityFieldRepo.DATE_TIME_FIELD, EntityFieldRepo.DECIMAL_FIELD, EntityFieldRepo.ENUM_FIELD)),
            Arrays.asList(1000006, "test6", false, "2022-01-01T18:00:31", 100002.11, "OQS")
        );

    public static List<IEntity> DEFAULT_DYNAMIC_ENTITIES =
        Arrays.asList(LONG_STRING_ENTITY_C0_1, LONG_STRING_ENTITY_C0_2, BOOL_DATE_ENTITY_C1_1, BOOL_DATE_ENTITY_C1_2, DECIMAL_STRINGS_ENTITY_C2_1, DECIMAL_STRINGS_ENTITY_C2_2);

    public static List<Tuple2<IEntity, List<Object>>> DEFAULT_STATIC_ENTITIES = Arrays.asList(STATIC_ENTITY_1, STATIC_ENTITY_2, STATIC_ENTITY_3, STATIC_ENTITY_4, STATIC_ENTITY_5, STATIC_ENTITY_6);
}
