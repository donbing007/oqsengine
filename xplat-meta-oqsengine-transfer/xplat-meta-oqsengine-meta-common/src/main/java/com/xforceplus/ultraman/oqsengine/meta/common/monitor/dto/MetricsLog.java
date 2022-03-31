package com.xforceplus.ultraman.oqsengine.meta.common.monitor.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by justin.xu on 12/2021.
 *
 * @since 1.8
 */
public class MetricsLog {
    private String appId;
    private String clientId;
    private String code;
    private Message message;

    public MetricsLog(String appId, String code, Message message) {
        this.appId = appId;
        this.code = code;
        this.message = message;
    }

    public MetricsLog(String appId, String clientId, String code, Message message) {
        this.appId = appId;
        this.clientId = clientId;
        this.code = code;
        this.message = message;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Message getMessage() {
        return message;
    }


    /**
     * 内部记录错误发生的时间、错误信息
     */
    public static class Message {
        private long timeStamp;
        private String message;

        public Message(String message) {
            this.message = message;
            this.timeStamp = System.currentTimeMillis();
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        public String getMessage() {
            return message;
        }
    }

    public static enum ShowType {
        ALL,
        INFO,
        ERROR;

        public static ShowType getInstance(String type) {
            if (null != type) {
                for (ShowType showType : ShowType.values()) {
                    if (showType.name().equals(type.toUpperCase())) {
                        return showType;
                    }
                }
            }

            return ALL;
        }
    }

    public static List<MetricsLog> toMetricsLogs(Map<String, Map<String, Message>> raw) {
        List<MetricsLog> metricsLogs = new ArrayList<>();
        raw.forEach(
            (key, value) -> {
                value.forEach(
                    (code, error) -> {
                        MetricsLog metricsLog;
                        if (code.contains(MetricsLog.LINK)) {
                            String[] sp = code.split(MetricsLog.LINK);
                            if (sp.length > 1) {
                                metricsLog = new MetricsLog(key, sp[0], sp[1], error);
                            } else {
                                metricsLog = new MetricsLog(key, code, error);
                            }
                        }  else {
                            metricsLog = new MetricsLog(key, code, error);
                        }
                        metricsLogs.add(metricsLog);
                    }
                );
            }
        );

        return metricsLogs;
    }

    public static final String LINK = "__";


    public static String linkKey(String... keys) {
        if (null == keys || 0 == keys.length) {
            return "";
        }

        if (1 == keys.length) {
            return keys[0];
        }

        return String.join(LINK, keys);
    }
}
