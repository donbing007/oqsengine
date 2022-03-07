package com.xforceplus.ultraman.oqsengine.cdc.consumer.tools;

import com.xforceplus.ultraman.oqsengine.cdc.consumer.dto.ParseResult;
import com.xforceplus.ultraman.oqsengine.cdc.context.ParserContext;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Created by justin.xu on 01/2022.
 *
 * @since 1.8
 */
public class CommonUtils {

    /**
     * 获取entityClass.
     *
     * @param id 当前记录id.
     * @param entityClassRef
     * @param parserContext
     * @return
     * @throws SQLException
     */
    public static IEntityClass getEntityClass(long id, EntityClassRef entityClassRef, ParserContext parserContext) throws
        SQLException {

        Optional<IEntityClass>
            entityClassOptional = parserContext.getMetaManager().load(entityClassRef.getId(), entityClassRef.getProfile());

        if (entityClassOptional.isPresent()) {

            return entityClassOptional.get();
        }

        throw new SQLException(
            String.format("[common-utils] id : %d has no entityClass...", id));
    }

    private static String toClassKeyWithProfile(long id, String profile) {
        return id + "_" + (null == profile ? "" : profile);
    }
}
