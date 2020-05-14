package com.xforceplus.ultraman.oqsengine.sdk.event;

import com.xforceplus.ultraman.metadata.grpc.Base;
import com.xforceplus.ultraman.metadata.grpc.ModuleUpResult;

import java.util.List;

/**
 * a module is got
 */
public class MetadataModuleGotEvent {

    private Base.Authorization request;

    private List<ModuleUpResult> modules;

    public MetadataModuleGotEvent(Base.Authorization request, List<ModuleUpResult> modules) {
        this.request = request;
        this.modules = modules;
    }

    public Base.Authorization getRequest() {
        return request;
    }

    public List<ModuleUpResult> getResponse() {
        return modules;
    }


    @Override
    public String toString() {
        return "MetadataModuleGotEvent{" +
                "request=" + request +
                ", modules=" + modules +
                '}';
    }
}
