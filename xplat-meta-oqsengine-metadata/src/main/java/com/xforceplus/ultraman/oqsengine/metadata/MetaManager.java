package com.xforceplus.ultraman.oqsengine.metadata;

import com.xforceplus.ultraman.oqsengine.meta.common.monitor.dto.MetricsLog;
import com.xforceplus.ultraman.oqsengine.metadata.dto.metrics.AppSimpleInfo;
import com.xforceplus.ultraman.oqsengine.metadata.dto.metrics.MetaMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
     * @param version       版本.
     * @param profile       个性化定制标识.
     * @return 元信息的实例.
     */
    Optional<IEntityClass> load(long entityClassId, int version, String profile);

    /**
     * 获取当前entityClassId下的所有entityClass, entityClass profiles.
     *
     * @param entityClassId 元信息标识.
     * @return 元信息的实例集合.
     */
    Collection<IEntityClass> withProfilesLoad(long entityClassId);

    /**
     * 表示需要关注此appId代表的应用的元信息.
     *
     * @param appId 应用标识.
     * @return 当前的元信息版本号.小于0表示没有持有任何版本的元信息.
     */
    int need(String appId, String env);

    /**
     * 表示需要关注此appId代表的应用的元信息.
     *
     * @param appId 应用标识.
     * @param reset 是否为重置.
     * @return 当前的元信息版本号.小于0表示没有持有任何版本的元信息.
     */
    int need(String appId, String env, boolean reset);

    /**
     * 清空本地缓存.
     * 这个操作将强制将本地缓存清除.
     */
    void invalidateLocal();

    /**
     * 导入Meta信息.
     *
     * @param appId   应用ID.
     * @param env     环境CODE.
     * @param version 应用版本.
     * @param content 应用的entityClass json.
     */
    boolean metaImport(String appId, String env, int version, String content);

    /**
     * 维护类接口, 查询当前的meta指标.
     *
     * @param appId 应用ID.
     * @return MetaMetrics指标.
     */
    Optional<MetaMetrics> showMeta(String appId) throws Exception;

    /**
     * 查看当前的meta日志.
     *
     * @param showType 类型 info/error/all.
     * @return 日志集合.
     */
    default Collection<MetricsLog> metaLogs(MetricsLog.ShowType showType) {
        return new ArrayList<>();
    }

    /**
     * 表示将刷新某个appId所关注的env信息.
     *
     * @param appId 应用标识.
     * @param env   环境标识.
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

    /**
     * 显示当前oqs中所有正在使用的appId.
     *
     * @return appId列表.
     */
    default List<AppSimpleInfo> showApplications() {
        return new ArrayList<>();
    }
}
