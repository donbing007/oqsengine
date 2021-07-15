package com.xforceplus.ultraman.oqsengine.metadata.dto.metrics;


import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.EntityClassStorage;
import java.util.Collection;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
public class MetaMetrics {
    private int version;
    private String env;
    private String appId;

    private Collection<EntityClassStorage> metas;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Collection<EntityClassStorage> getMetas() {
        return metas;
    }

    public void setMetas(Collection<EntityClassStorage> metas) {
        this.metas = metas;
    }

    /**
     * construct method.
     */
    public MetaMetrics(int version, String env, String appId,
                       Collection<EntityClassStorage> metas) {
        this.version = version;
        this.env = env;
        this.appId = appId;
        this.metas = metas;
    }
}
