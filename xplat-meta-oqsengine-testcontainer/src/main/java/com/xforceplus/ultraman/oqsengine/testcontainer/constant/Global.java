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
    public static final Network NETWORK = Network.newNetwork();

    public static final ConcurrentHashMap<ContainerSupport, ContainerWrapper> CONTAINER_MAP = new ConcurrentHashMap();
    public static volatile boolean HOOKED = false;
    public static final Object LOCK = new Object();

    public static final int WAIT_START_TIME_OUT = 200;

    public Global() {
    }
}
