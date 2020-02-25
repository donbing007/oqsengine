package com.xforceplus.ultraman.oqsengine.sdk.store.repository;

import com.xforceplus.ultraman.oqsengine.pojo.dto.UltForm;
import com.xforceplus.ultraman.oqsengine.sdk.store.MapLocalStore;
import org.apache.metamodel.delete.DeleteFrom;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * formBo的存储
 */
public class FormBoMapLocalStore extends MapLocalStore {

    public static FormBoMapLocalStore create(){
        return new FormBoMapLocalStore("formBos", "formBo",
                new String[]{"id", "appId", "name", "code", "refFormId","tenantId", "tenantName", "setting", "version"}
        , null, false, null);
    }

    private FormBoMapLocalStore(String schema, String tableName, String[] columns, String[] pkColumns, boolean hasVersion, Comparator<Object> versionComparator) {
        super(schema, tableName, columns, pkColumns, hasVersion, versionComparator);
    }

    public void save(UltForm ultForm){
        dc.executeUpdate(new DeleteFrom(getTable()).where("id").eq(ultForm.getId()));
        Map<String, Object> map = new HashMap<>();
        map.put("id", ultForm.getId());
        map.put("appId", ultForm.getAppId());
        map.put("name", ultForm.getName());
        map.put("code", ultForm.getCode());
        map.put("refFormId",ultForm.getRefFormId());
        map.put("tenantId", ultForm.getTenantId());
        map.put("tenantName", ultForm.getTenantName());
        map.put("setting", ultForm.getSetting());
        map.put("version", ultForm.getVersion());
        this.save(map);
    }
}
