package com.xforceplus.ultraman.oqsengine.sdk.event;

import com.xforceplus.ultraman.metadata.grpc.Base;
import com.xforceplus.ultraman.metadata.grpc.ModuleUpResult;

/**
 * a module is got
 */
public class MetadataModuleGotEvent {

    private Base.Authorization request;

    private ModuleUpResult response;

    public MetadataModuleGotEvent(Base.Authorization request, ModuleUpResult response) {
        this.request = request;
        this.response = response;
    }

    public Base.Authorization getRequest() {
        return request;
    }

    public ModuleUpResult getResponse() {
        return response;
    }

    @Override
    public String toString() {
        return "MetadataModuleGotEvent{" +
                "request=" + request +
                ", response=" + response +
                '}';
    }
}
