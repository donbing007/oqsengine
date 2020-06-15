package com.xforceplus.ultraman.oqsengine.sdk.event.config;

import com.xforceplus.ultraman.config.event.ChangeList;

/**
 * change event
 */
public class ConfigChangeEvent {

    private String type;
    private ChangeList changeList;

    public ConfigChangeEvent(String type, ChangeList changeList) {
        this.type = type;
        this.changeList = changeList;
    }

    public String getType() {
        return type;
    }

    public ChangeList getChangeList() {
        return changeList;
    }

    @Override
    public String toString() {
        return "ConfigChangeEvent{" +
                "type='" + type + '\'' +
                ", changeList=" + changeList +
                '}';
    }
}
