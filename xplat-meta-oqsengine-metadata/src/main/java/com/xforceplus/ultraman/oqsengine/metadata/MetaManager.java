package com.xforceplus.ultraman.oqsengine.metadata;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;

import java.util.Optional;

/**
 * oqs 元数据管理
 *
 * @author dongbin
 * @version 0.1 2021/2/2 11:26
 * @since 1.8
 */
public interface MetaManager {

    /**
     * 加载指定的IEntityCalss实例.
     *
     * @param id 元信息的标识.
     * @return 元信息的实例.
     */
    Optional<IEntityClass> load(long id);

    /**
     * 表示需要关注此appid代表的应用的元信息.
     *
     * @param appId 应用标识.
     * @return 当前的元信息版本号.小于0表示没有持有任何版本的元信息.
     */
    int need(long appId);


}
