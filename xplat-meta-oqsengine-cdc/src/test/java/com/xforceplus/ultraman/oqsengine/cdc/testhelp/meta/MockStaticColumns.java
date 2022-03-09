package com.xforceplus.ultraman.oqsengine.cdc.testhelp.meta;


import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.Arrays;
import java.util.List;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class MockStaticColumns {

    public static final List<IEntityField> columns = Arrays.asList(
        EntityFieldRepo.ID_FIELD, EntityFieldRepo.LONG_FIELD, EntityFieldRepo.STRING_FIELD, EntityFieldRepo.BOOL_FIELD,
        EntityFieldRepo.DATE_TIME_FIELD, EntityFieldRepo.DECIMAL_FIELD, EntityFieldRepo.ENUM_FIELD
    );
}
