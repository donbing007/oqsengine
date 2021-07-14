package com.xforceplus.ultraman.oqsengine.meta.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
public class ClientIdUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(ClientIdUtils.class);

    private static final String DEFAULT_HOST_NAME = "OQS-ENGINE";

    public static String generate() {
        String clientId = "";
        try {
            String hostName = System.getenv("HOSTNAME");
            InetAddress ipHost = getInetAddress();
            //  ipHost is null
            if (null == ipHost) {
                //  host is null;
                if (null == hostName || hostName.isEmpty()) {
                    clientId = DEFAULT_HOST_NAME;
                }
                return clientId;
            } else if (null == hostName || hostName.isEmpty()) {
                hostName = ipHost.getHostName();
            }
            clientId = hostName + "/" + ipHost.getHostAddress();
            return clientId;
        } finally {
            LOGGER.info("generate clientId : {}", clientId);
        }
    }

    private static InetAddress getInetAddress() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ipHost = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ipHost = (InetAddress) addresses.nextElement();
                    if (ipHost instanceof Inet4Address) {
                        LOGGER.info("getInetAddress hostIp : {}, hostName : {}", ipHost.getHostAddress(), ipHost.getHostName());
                        return ipHost;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("get clientId ip/host failed, message {}.", e.getMessage());
        }

        return null;
    }
}
