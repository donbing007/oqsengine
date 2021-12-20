package com.xforceplus.ultraman.oqsengine.calculation.event.dto;

import com.xforceplus.ultraman.oqsengine.event.payload.calculator.AppMetaChangePayLoad;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by justin.xu on 12/2021.
 *
 * @since 1.8
 */
public class CalculationEvent {
    private String appId;
    private int version;
    private List<Long> appClassIds;
    private Map<Long, List<AppMetaChangePayLoad.FieldChange>> fieldChanges;

    /**
     * construct fx.
     */
    public CalculationEvent(String appId, int version, List<Long> appClassIds) {
        this.appId = appId;
        this.version = version;
        this.appClassIds = appClassIds;
        this.fieldChanges = new HashMap<>();
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setAppClassIds(List<Long> appClassIds) {
        this.appClassIds = appClassIds;
    }

    public String getAppId() {
        return appId;
    }

    public List<Long> getAppClassIds() {
        return appClassIds;
    }

    public Map<Long, List<AppMetaChangePayLoad.FieldChange>> getFieldChanges() {
        return fieldChanges;
    }
}
