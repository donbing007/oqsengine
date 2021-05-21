package com.xforceplus.ultraman.oqsengine.synchronizer.server.dto;

import java.util.Objects;

/**
 * lock state request.
 */
public class LockStateRequest {

    private String uuid;

    private String token;

    public LockStateRequest(String uuid, String token) {
        this.uuid = uuid;
        this.token = token;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LockStateRequest that = (LockStateRequest) o;
        return Objects.equals(uuid, that.uuid)
            && Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, token);
    }
}
