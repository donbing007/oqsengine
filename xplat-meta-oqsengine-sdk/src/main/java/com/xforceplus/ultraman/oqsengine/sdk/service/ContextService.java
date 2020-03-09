package com.xforceplus.ultraman.oqsengine.sdk.service;

public interface ContextService {

    interface ContextKey<T> {

        default Class<T> type(){
            return null;
        }

        String name();
    }

    enum StringKeys implements ContextKey<String> {

        TransactionKey("transaction-id"),

        TenantIdKey("tenant-id"),

        TenantCodeKey("tenant-code"),

        AppCode("app-code");

        private String keyName;

        StringKeys(String keyName){
            this.keyName = keyName;
        }

        public Class<String> type(){
            return String.class;
        }
    }

    /**
     * set attribute
     * @param key
     * @param value
     */
    <T> void set(ContextKey<T> key, T value);

    /**
     * get attribute
     * @param key
     * @param <T>
     * @return
     */
    <T> T get(ContextKey<T> key);
}