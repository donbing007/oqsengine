package com.xforceplus.ultraman.oqsengine.metadata.dto.model;

/**
 * Created by justin.xu on 12/2021.
 *
 * @since 1.8
 */
public abstract class AbstractMetaModel {
    private MetaModel model;

    public AbstractMetaModel(MetaModel metaModel) {
        this.model = metaModel;
    }

    public MetaModel getModel() {
        return model;
    }
}
