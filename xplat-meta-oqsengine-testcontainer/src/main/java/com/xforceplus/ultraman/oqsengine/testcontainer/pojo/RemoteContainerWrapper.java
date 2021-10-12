package com.xforceplus.ultraman.oqsengine.testcontainer.pojo;

/**
 * Created by justin.xu on 09/2021.
 *
 * @since 1.8
 */
public class RemoteContainerWrapper implements ContainerWrapper {
    private String host;
    private String port;

    public RemoteContainerWrapper() {
    }

    public RemoteContainerWrapper(String host, String port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public String host() {
        return host;
    }

    @Override
    public String port() {
        return port;
    }

}
