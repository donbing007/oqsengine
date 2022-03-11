package com.xforceplus.ultraman.oqsengine.cdc.testhelp.meta;

import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class EntityBuilder {

    public static IEntity buildEntity(long baseId, IEntityClass entityClass, List<IEntityField> fieldList) {
        IEntity entity = Entity.Builder.anEntity()
            .withId(baseId)
            .withMajor(OqsVersion.MAJOR)
            .withEntityClassRef(entityClass.ref())
            .withVersion(1)
            .build();

        if (entityClass.isDynamic()) {
            entity.entityValue().addValues(
                buildValue(fieldList)
            );
        }

        return entity;
    }

    private static Collection<IValue> buildValue(Collection<IEntityField> fields) {
        return fields.stream().map(f -> {
            switch (f.type()) {
                case STRING: {
                    String randomString = buildRandomString(10);
                    return new StringValue(f, randomString, randomString);
                }
                case STRINGS: {
                    String randomString0 = buildRandomString(5);
                    String randomString1 = buildRandomString(3);
                    String randomString2 = buildRandomString(7);
                    return new StringsValue(f, new String[] { randomString0, randomString1, randomString2}, randomString0);
                }
                default: {
                    long randomLong = buildRandomLong(10, 100000);
                    return new LongValue(f, randomLong, Long.toString(randomLong));
                }
            }
        }).collect(Collectors.toList());
    }

    private static String buildRandomString(int size) {
        StringBuilder buff = new StringBuilder();
        Random rand = new Random(47);
        for (int i = 0; i < size; i++) {
            buff.append(rand.nextInt(26) + 'a');
        }
        return buff.toString();
    }

    private static int buildRandomLong(int min, int max) {
        Random random = new Random();

        return random.nextInt(max) % (max - min + 1) + min;
    }
}
