package com.xforceplus.ultraman.oqsengine.sdk.event;

import com.xforceplus.ultraman.metadata.grpc.Base;
import com.xforceplus.ultraman.metadata.grpc.ModuleUpResult;

public class MetadataModuleGetEvent {

    private Base.Authorization request;

    private ModuleUpResult response;

    public MetadataModuleGetEvent(Base.Authorization request, ModuleUpResult response) {
        this.request = request;
        this.response = response;
    }

    public Base.Authorization getRequest() {
        return request;
    }

    public ModuleUpResult getResponse() {
        return response;
    }
}
