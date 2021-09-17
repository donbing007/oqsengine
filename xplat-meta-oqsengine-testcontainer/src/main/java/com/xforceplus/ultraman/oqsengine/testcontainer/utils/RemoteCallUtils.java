package com.xforceplus.ultraman.oqsengine.testcontainer.utils;

import com.xforceplus.ultraman.oqsengine.testcontainer.enums.ContainerSupport;
import com.xforceplus.ultraman.oqsengine.testcontainer.pojo.RemoteContainerWrapper;
import com.xforceplus.ultraman.oqsengine.testcontainer.utils.rest.RestSender;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;

/**
 * Created by justin.xu on 09/2021.
 *
 * @since 1.8
 */
public class RemoteCallUtils {
    public static final String HOST  = "localhost";
    public static int PORT = 9898;
    public static final Map<ContainerSupport, RemoteContainerWrapper> REMOTE_CONTAINER_PROPERTIES = new HashMap<>();

    public static RemoteContainerWrapper startUseRemoteContainer(String uuid, ContainerSupport containerSupport) {
        RemoteContainerWrapper remoteContainerWrapper = REMOTE_CONTAINER_PROPERTIES.get(containerSupport);
        if (null != remoteContainerWrapper) {
            return remoteContainerWrapper;
        }

        Map<ContainerSupport, Integer> result = RestSender.gets(defaultURL("init", uuid)
            , new ParameterizedTypeReference<Map<ContainerSupport, Integer>>(){});

        if (null != result && !result.isEmpty()) {
            result.forEach(
                (k,v) -> {
                    REMOTE_CONTAINER_PROPERTIES.put(k, new RemoteContainerWrapper(HOST, v.toString()));
                }
            );

        }

        return REMOTE_CONTAINER_PROPERTIES.get(containerSupport);
    }

    public static void refreshUseRemoteContainer(String uuid) {
        RestSender.get(defaultURL("refresh", uuid), Boolean.class);
    }

    public static void finishUseRemoteContainer(String uuid) {
        RestSender.get(defaultURL("destroy", uuid), Boolean.class);
    }

    private static String defaultURL(String action, String uuid) {
        String temp = System.getProperty("container.server.port");
        if (null != temp) {
            try {
                Integer integer = Integer.parseInt(temp);
                if (null != integer) {
                    PORT = integer;
                }
            } catch (Exception e) {
                //  ignore
            }
        }
        return "http://" + HOST + ":" + PORT + "/container-providers/" + action + "/" + uuid;
    }
}
