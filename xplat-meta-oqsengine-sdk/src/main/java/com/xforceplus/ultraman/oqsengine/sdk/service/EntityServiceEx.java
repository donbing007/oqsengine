package com.xforceplus.ultraman.oqsengine.sdk.service;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.DictItem;
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
     *
     * @param pageCode
     * @param tenantCode
     * @return
     */
    List<UltPageBoItem> findPageBos(String pageCode, String tenantCode);

    /**
     * 根据字典ID查找字典信息，根据字典id+code查找具体的字典项
     * @param enumId
     * @param enumCode
     * @return
     */
    List<DictItem> findDictItems(String enumId, String enumCode);

    /**
     * 根据字典code查找字典信息，根据code+enumCode查找字典项
     * @param code
     * @param enumCode
     * @return
     */
    List<DictItem> findDictItemsByCode(String code, String enumCode);

}
