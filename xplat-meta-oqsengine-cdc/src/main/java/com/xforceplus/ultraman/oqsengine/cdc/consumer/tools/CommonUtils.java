package com.xforceplus.ultraman.oqsengine.cdc.consumer.tools;

import com.xforceplus.ultraman.oqsengine.cdc.context.ParserContext;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by justin.xu on 01/2022.
 *
 * @since 1.8
 */
public class CommonUtils {

    /**
     * 获取entityClass.
     */
    public static IEntityClass getEntityClass(EntityClassRef entityClassRef, ParserContext parserContext) throws
        SQLException {

        Optional<IEntityClass>
            entityClassOptional =
            parserContext.getMetaManager().load(entityClassRef.getId(), entityClassRef.getProfile());

        if (entityClassOptional.isPresent()) {

            return entityClassOptional.get();
        }

        throw new SQLException(
            String.format("[common-utils] id : %d, profile : %s has no entityClass...",
                entityClassRef.getId(), entityClassRef.getProfile()));
    }

    /**
     * toErrorCommitIdStr.
     */
    public static String toErrorCommitIdStr(Set<Long> commitIds, Set<Long> unCommitIds) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(commitIds);
        if (!unCommitIds.isEmpty()) {
            stringBuilder.append(unCommitIds);
        }

        return stringBuilder.toString();
    }
}
