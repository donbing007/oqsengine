package com.xforceplus.ultraman.oqsengine.core.service;

import com.xforceplus.ultraman.oqsengine.core.metadata.IEntity;

/**
 * entity 管理服务.
 * @author dongbin
 * @version 0.1 2020/2/17 20:38
 * @since 1.8
 */
public interface EntityManagementService {

    /**
     * 创建一个新的 entity 实例.
     * @param entity 目标 entity 数据.
     * @return 新对象的标识.
     */
    long build(IEntity entity);

    /**
     * 替换一个已经存在的 entity 的信息.
     * @param entity 目标 entity.
     */
    void replace(IEntity entity);

    /**
     * 删除一个已经存在的 entity.
     * @param entity 目标 entity.
     */
    void delete(IEntity entity);
}
