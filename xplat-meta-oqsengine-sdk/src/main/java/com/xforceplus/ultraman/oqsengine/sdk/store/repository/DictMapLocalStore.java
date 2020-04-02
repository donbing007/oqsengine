package com.xforceplus.ultraman.oqsengine.sdk.store.repository;

import com.xforceplus.ultraman.metadata.grpc.DictUpResult;
import com.xforceplus.ultraman.oqsengine.sdk.store.MapLocalStore;
import org.apache.metamodel.delete.DeleteFrom;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * dict map store
 */
public class DictMapLocalStore extends MapLocalStore {

    public static DictMapLocalStore create() {
        return new DictMapLocalStore("dicts", "dict", new String[]{"name", "dictId", "dictCode", "dictName", "publishDictId", "tenantId", "appId", "code", "version"}
            , null, false, null);
    }

    private DictMapLocalStore(String schema, String tableName, String[] columns, String[] pkColumns, boolean hasVersion, Comparator<Object> versionComparator) {
        super(schema, tableName, columns, pkColumns, hasVersion, versionComparator);
    }

    public void save(DictUpResult dictUpResult, String tenantId, String appId) {
        dictUpResult.getDictsList().forEach(dict -> {

            //remove all id related
            dc.executeUpdate(new DeleteFrom(getTable()).where("dictId").eq(dict.getId()));

            dict.getDictUpDetailsList().forEach(details -> {
                //record
                Map<String, Object> map = new HashMap<>();
                map.put("tenantId", tenantId);
                map.put("appId", appId);
                map.put("dictId", dict.getId());
                map.put("dictCode", dict.getCode());
                map.put("dictName", dict.getName());
                map.put("publishDictId", dict.getPublishDictId());
                map.put("code", details.getCode());
                map.put("name", details.getName());
                map.put("version", dict.getVersion());
                this.save(map);
            });
        });
    }
}
