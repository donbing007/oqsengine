package com.xforceplus.ultraman.oqsengine.metadata;

import com.xforceplus.ultraman.oqsengine.meta.common.monitor.dto.MetricsLog;
import com.xforceplus.ultraman.oqsengine.metadata.dto.metrics.MetaMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import java.util.ArrayList;
import java.util.Collection;
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
     * 加载指定的IEntityClass实例.
     *
     * @param appId 当前应用ID.
     * @return 元信息的实例集合.
     */
    default Collection<IEntityClass> appLoad(String appId) {
        return new ArrayList<>();
    }

    /**
     * 加载指定的IEntityClass实例.
     *
     * @param entityClassId 元信息标识.
     * @param profile       个性化定制标识.
     * @return 元信息的实例.
     */
    Optional<IEntityClass> load(long entityClassId, String profile);

    /**
     * 加载指定的IEntityClass实例.
     *
     * @param ref entityClass指针.
     * @return 元信息.
     */
    default Optional<IEntityClass> load(EntityClassRef ref) {
        return load(ref.getId(), ref.getProfile());
    }

    /**
     * 加载指定的IEntityClass实例.
     *
     * @param entityClassId 元信息标识.
     * @param version 版本.
     * @param profile 个性化定制标识.
     * @return 元信息的实例.
     */
    Optional<IEntityClass> load(long entityClassId, int version, String profile);

    /**
     * 获取当前entityClassId下的所有EntityClassWithProfile.
     *
     * @param entityClassId 元信息标识.
     * @return 元信息的实例.
     */
    Collection<IEntityClass> withProfilesLoad(long entityClassId, int version);

    /**
     * 表示需要关注此appId代表的应用的元信息.
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
     * 导入Meta信息.
     */
    boolean metaImport(String appId, String env, int version, String content);

    /**
     * 产看当前appId下的信息.
     */
    Optional<MetaMetrics> showMeta(String appId) throws Exception;

    /**
     * 查询同步日志.
     */
    default Collection<MetricsLog> metaLogs(MetricsLog.ShowType showType) {
        return new ArrayList<>();
    }

    /**
     * 表示将刷新某个appId所关注的env信息.
     *
     * @param appId 应用标识.
     * @return 当前的元信息版本号, 小于0表示没有持有任何版本的元信息.
     */
    int reset(String appId, String env);

    /**
     * 清除某个appId下的所有缓存信息.
     *
     * @param appId 业务ID.
     * @return true, false.
     */
    boolean remove(String appId);
}
