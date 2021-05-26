package com.xforceplus.ultraman.oqsengine.synchronizer.server.dto;

import com.xforceplus.ultraman.oqsengine.synchronizer.server.CriticalResource;

/**
 * a pojo represent a critical resource.
 */
public class CriticalResourceKey {

    private CriticalResource.ResType resType;

    private Object res;

    public CriticalResourceKey(CriticalResource.ResType resType, Object res) {
        this.resType = resType;
        this.res = res;
    }

    public CriticalResource.ResType getResType() {
        return resType;
    }

    public Object getRes() {
        return res;
    }
}
