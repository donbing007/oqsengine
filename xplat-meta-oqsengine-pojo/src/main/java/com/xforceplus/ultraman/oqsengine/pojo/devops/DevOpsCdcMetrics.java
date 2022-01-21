package com.xforceplus.ultraman.oqsengine.pojo.devops;

/**
 * Created by justin.xu on 01/2022.
 *
 * @since 1.8
 */
public class DevOpsCdcMetrics {
    private int success;
    private int fails;

    public DevOpsCdcMetrics() {
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFails() {
        return fails;
    }

    public void setFails(int fails) {
        this.fails = fails;
    }

    public void incrementByStatus(boolean isSuccess) {
        if (isSuccess) {
            success++;
        } else {
            fails++;
        }
    }

    public void allFails() {
        fails += success;
        success = 0;
    }
}

