package com.xforceplus.ultraman.oqsengine.metadata;

import com.xforceplus.ultraman.oqsengine.metadata.dto.metrics.MetaMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import java.util.Optional;

/**
 * oqs 元数据管理.
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
     * 加载指定的IEntityCalss实例.
     *
     * @param id      元信息标识.
     * @param profile 个性化定制标识.
     * @return 元信息的实例.
     */
    Optional<IEntityClass> load(long id, String profile);

    /**
     * 加载指定的IEntityCalss + version实例.
     *
     * @param id 元信息的标识.
     * @return 元信息的实例.
     */
    Optional<IEntityClass> loadHistory(long id, int version);

    /**
     * 表示需要关注此appid代表的应用的元信息.
     *
     * @param appId 应用标识.
     * @return 当前的元信息版本号.小于0表示没有持有任何版本的元信息.
     */
    int need(String appId, String env);

    /**
     * 清空本地缓存.
     * 这个操作将强制将本地缓存清除.
     */
    void invalidateLocal();

    /**
     * 导入.
     */
    boolean dataImport(String appId, String env, int version, String content);

    /**
     * 产看当前appId下的信息.
     */
    Optional<MetaMetrics> showMeta(String appId) throws Exception;
}
