package com.xforceplus.ultraman.oqsengine.sdk.service;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.UltPageBoItem;
import io.vavr.control.Either;

import java.util.List;
import java.util.Map;

/**
 * extend entity service
 */
public interface EntityServiceEx {

    Either<String, IEntity> create(EntityClass entityClass, Map<String, Object> inputValue);

    Either<String, Map<String, Object>> findOneByParentId(EntityClass entityClass, EntityClass subEntityClass, long id);

    /**
     * 查找页面的BO列表，先从租户上找，如果没有则返回默认
     * @param pageCode
     * @param tenantId
     * @return
     */
    List<UltPageBoItem> findPageBos(String pageCode,String tenantId);

}
