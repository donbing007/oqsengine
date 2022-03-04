package com.xforceplus.ultraman.oqsengine.metadata.dto.model;

/**
 * Created by justin.xu on 12/2021.
 *
 * @since 1.8
 */
public class OfflineModel extends AbstractMetaModel {

    private String path;

    public OfflineModel(String path) {
        super(MetaModel.OFFLINE);
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
