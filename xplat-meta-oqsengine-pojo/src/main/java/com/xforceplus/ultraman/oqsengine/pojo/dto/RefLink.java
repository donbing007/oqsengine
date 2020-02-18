package com.xforceplus.ultraman.oqsengine.pojo.dto;

import com.xforceplus.ultraman.oqsengine.core.metadata.IEntityClass;
import com.xforceplus.ultraman.oqsengine.core.metadata.IEntityValue;
import com.xforceplus.ultraman.oqsengine.core.metadata.ILink;

public class RefLink implements ILink {

    @Override
    public IEntityValue searchValue(IEntityClass entityClass, Long id, String version) {
        return null;
    }
}
