package com.xforceplus.ultraman.oqsengine.sdk.store.repository;

import com.xforceplus.ultraman.oqsengine.pojo.dto.UltPage;
import com.xforceplus.ultraman.oqsengine.sdk.store.MapLocalStore;
import org.apache.metamodel.delete.DeleteFrom;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * pageBo的存储
 */
public class PageBoMapLocalStore extends MapLocalStore {

    public static PageBoMapLocalStore create() {
        return new PageBoMapLocalStore("pageBos", "pageBo",
                new String[]{"settingId", "id", "appId", "name", "code", "refPageId", "tenantId", "tenantName", "tenantCode", "version", "boName", "boCode", "setting", "remark", "envStatus"}
                , null, false, null);
    }

    private PageBoMapLocalStore(String schema, String tableName, String[] columns, String[] pkColumns, boolean hasVersion, Comparator<Object> versionComparator) {
        super(schema, tableName, columns, pkColumns, hasVersion, versionComparator);
    }

    public void save(UltPage ultPage) {
        ultPage.getPageBoVos().forEach(ultPageBo -> {
            //有配置数据才保存
            //删除重复的先
            dc.executeUpdate(new DeleteFrom(getTable()).where("settingId").eq(ultPageBo.getSettingId()));
            //如果有相同code的但是版本不同的记录对它的状态进行删除
            if (!StringUtils.isEmpty(ultPage.getCode())) {
                dc.executeUpdate(new DeleteFrom(getTable()).where("code").eq(ultPage.getCode())
                        .where("version").ne(ultPage.getVersion()));
            }

            Map<String, Object> map = new HashMap<>();
            map.put("settingId", ultPageBo.getSettingId());
            map.put("id", ultPage.getId());
            map.put("appId", ultPage.getAppId());
            map.put("name", ultPage.getName());
            map.put("code", ultPage.getCode());
            map.put("refPageId", ultPage.getRefPageId());
            map.put("tenantId", ultPage.getTenantId());
            map.put("tenantCode", ultPage.getTenantCode());
            map.put("tenantName", ultPage.getTenantName());
            map.put("version", ultPage.getVersion());
            map.put("boName", ultPageBo.getBoName());
            map.put("boCode", ultPageBo.getBoCode());
            map.put("setting", ultPageBo.getSetting());
            map.put("remark", ultPageBo.getRemark());
            map.put("envStatus", ultPage.getEnvStatus());
            this.save(map);
        });
    }
}
