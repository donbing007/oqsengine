package com.xforceplus.ultraman.oqsengine.testcontainer.constant;

import com.xforceplus.ultraman.oqsengine.testcontainer.enums.ContainerSupport;
import com.xforceplus.ultraman.oqsengine.testcontainer.pojo.ContainerWrapper;
import java.util.concurrent.ConcurrentHashMap;
import org.testcontainers.containers.Network;

/**
 * Created by justin.xu on 09/2021.
 *
 * @since 1.8
 */
public class Global {
    public static final Network NETWORK = Network.NetworkImpl.builder().createNetworkCmdModifier(
        createNetworkCmd -> {
            com.github.dockerjava.api.model.Network.Ipam.Config config =
                new com.github.dockerjava.api.model.Network.Ipam.Config();
            com.github.dockerjava.api.model.Network.Ipam ipam = new com.github.dockerjava.api.model.Network.Ipam();
            ipam.withConfig(config.withSubnet("10.10.10.0/16"));
            createNetworkCmd.withIpam(ipam);
        }).build();

    public static final ConcurrentHashMap<ContainerSupport, ContainerWrapper> CONTAINER_MAP = new ConcurrentHashMap();
    public static volatile boolean HOOKED = false;
    public static final Object LOCK = new Object();

    public static final int WAIT_START_TIME_OUT = 200;
    public Global() {
    }
}
