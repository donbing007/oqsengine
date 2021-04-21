package com.xforceplus.ultraman.oqsengine.common.cdc;

/**
 * Created by justin.xu on 04/2021
 */
public class SkipRow {

    public static String toSkipRow(long commitId, long id, int version, int op) {
        return String.format("%d.%d.%d.%d", commitId, id, version, op);
    }

    /**
     * enum
     */
    public enum Status {
        ERROR_RECORD("1"),
        NOT_RECORD("0");

        private String status;

        Status(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }
}
