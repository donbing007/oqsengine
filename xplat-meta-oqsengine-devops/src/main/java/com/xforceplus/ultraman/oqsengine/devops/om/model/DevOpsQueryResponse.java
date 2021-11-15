package com.xforceplus.ultraman.oqsengine.devops.om.model;

import java.util.Collection;
import java.util.Map;

/**
 * @copyright： 上海云砺信息科技有限公司
 * @author: youyifan
 * @createTime: 11/3/2021 4:45 PM
 * @description:
 * @history:
 */
public class DevOpsQueryResponse {

    DevOpsQuerySummary summary;

    Collection<Map> rows;

    public DevOpsQuerySummary getSummary() {
        return summary;
    }

    public void setSummary(DevOpsQuerySummary summary) {
        this.summary = summary;
    }

    public Collection<Map> getRows() {
        return rows;
    }

    public void setRows(Collection<Map> rows) {
        this.rows = rows;
    }
}
