package com.xforceplus.ultraman.oqsengine.testcontainer.pojo;

import org.testcontainers.containers.GenericContainer;

/**
 * Created by justin.xu on 09/2021.
 *
 * @since 1.8
 */
public class FixedContainerWrapper implements ContainerWrapper {

    private GenericContainer genericContainer;

    public FixedContainerWrapper(GenericContainer genericContainer) {
        this.genericContainer = genericContainer;
    }

    @Override
    public String host() {
        return genericContainer.getContainerIpAddress();
    }

    @Override
    public String port() {
        return genericContainer.getFirstMappedPort().toString();
    }

    public GenericContainer getGenericContainer() {
        return genericContainer;
    }
}
