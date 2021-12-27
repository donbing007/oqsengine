package com.xforceplus.ultraman.oqsengine.metadata.cache;

import static com.xforceplus.ultraman.oqsengine.metadata.mock.generator.GeneralEntityClassStorageBuilder.autoFill;
import static com.xforceplus.ultraman.oqsengine.metadata.mock.generator.GeneralEntityClassStorageBuilder.fieldConfig;
import static com.xforceplus.ultraman.oqsengine.metadata.mock.generator.GeneralEntityClassStorageBuilder.formula;
import static com.xforceplus.ultraman.oqsengine.metadata.mock.generator.GeneralEntityClassStorageBuilder.staticCalculation;

import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.GeneralConstant;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.GeneralEntityUtils;
import com.xforceplus.ultraman.oqsengine.pojo.define.OperationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import java.util.ArrayList;

/**
 * Created by justin.xu on 12/2021.
 * 这个类主要帮助生成metaPayLoad CREATE/UPDATE/DELETE测试所需要的一些数据.
 *
 * @since 1.8
 */
public class MetaPayLoadHelper {

    /**
     * 产生一个基本对象.
     * @param id entityClassId.
     * @return EntityClassStorage.
     */
    public static EntityClassStorage toBasicPrepareEntity(long id) {
        EntityClassStorage entityClassStorage = new EntityClassStorage();
        entityClassStorage.setId(id);
        entityClassStorage.setVersion(GeneralConstant.DEFAULT_VERSION);

        entityClassStorage.setName(id + GeneralConstant.LEVEL
            + entityClassStorage.getLevel() + GeneralConstant.NAME_SUFFIX);

        entityClassStorage.setCode(id + GeneralConstant.LEVEL
            + entityClassStorage.getLevel() + GeneralConstant.CODE_SUFFIX);

        entityClassStorage.setFields(new ArrayList<>());
        entityClassStorage.setRelations(new ArrayList<>());

        return entityClassStorage;
    }

    public static EntityField genericEntityField(long id, FieldType fieldType, CalculationType calculationType, OperationType op) {
            EntityField.Builder builder = EntityField.Builder.anEntityField()
                .withId(id)
                .withName(GeneralEntityUtils.EntityFieldHelper.name(id))
                .withCnName(GeneralEntityUtils.EntityFieldHelper.cname(id))
                .withDictId(GeneralEntityUtils.EntityFieldHelper.dictId(id))
                .withFieldType(fieldType);

            switch (calculationType) {
                case STATIC: {
                    builder.withConfig(fieldConfig(staticCalculation()));
                    break;
                }
                case FORMULA: {
                    String express = GeneralConstant.MOCK_EXPRESSION;
                    if (op.equals(OperationType.UPDATE)) {
                        express = GeneralConstant.MOCK_EXPRESSION_SUB;
                    }
                    builder.withConfig(fieldConfig(formula(express, GeneralConstant.MOCK_LEVEL, fieldType)));
                    break;
                }
                case AUTO_FILL: {
                    String express = GeneralConstant.MOCK_PATTEN;
                    if (op.equals(OperationType.UPDATE)) {
                        express = GeneralConstant.MOCK_PATTEN_SUB;
                    }
                    builder.withConfig(fieldConfig(autoFill(express,
                        GeneralConstant.MOCK_MODEL, GeneralConstant.MOCK_MIN, GeneralConstant.MOCK_STEP)));
                    break;
                }
                default: {
                    throw new IllegalArgumentException("not support generate unknown-type calculator");
                }
            }

            return builder.build();
    }
}
