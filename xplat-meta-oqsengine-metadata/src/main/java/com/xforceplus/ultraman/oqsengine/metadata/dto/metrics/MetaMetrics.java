package com.xforceplus.ultraman.oqsengine.metadata.dto.metrics;


import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.EntityClassStorage;
import java.util.Collection;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
public class MetaMetrics extends MetaBase {

    private Collection<EntityClassStorage> metas;

    public MetaMetrics() {
        super();
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

        super(version, env, appId);

        this.metas = metas;
    }
}
