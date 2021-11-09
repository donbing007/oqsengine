package com.xforceplus.ultraman.oqsengine.metadata.handler;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import java.io.Serializable;
import java.util.Optional;

/**
 * Created by justin.xu on 11/2021.
 *
 * @since 1.8
 */
public interface EntityClassFormatHandler extends Serializable {

    Optional<IEntityClass> classLoad(long id, String profile);
}
