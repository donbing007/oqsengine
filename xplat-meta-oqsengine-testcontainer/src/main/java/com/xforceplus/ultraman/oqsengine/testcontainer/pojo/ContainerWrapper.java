package com.xforceplus.ultraman.oqsengine.testcontainer.pojo;

/**
 * Created by justin.xu on 09/2021.
 *
 * @since 1.8
 */
public interface ContainerWrapper {

    /**
     * 获取当前container的host.
     * @return
     */
    public String host();

    /**
     * 获取当前container的port.
     * @return
     */
    public String port();
}
