package com.xforceplus.ultraman.oqsengine.boot.grpc.devops;

import com.xforceplus.ultraman.devops.service.custom.pojo.dto.SDKAgentConfig;
import com.xforceplus.ultraman.devops.service.sdk.annotation.DiscoverService;
import com.xforceplus.ultraman.devops.service.transfer.generate.Application;
import com.xforceplus.ultraman.devops.service.transfer.generate.Service;
import com.xforceplus.ultraman.oqsengine.boot.config.system.SystemInfoConfiguration;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
@Component
public class DiscoverConfigService {

    @Autowired
    private MetaManager metaManager;

    @Autowired
    private SystemInfoConfiguration systemInfoConfiguration;

    /**
     * 发现服务,获取OQS的配置.
     *
     * @return 配置服务.
     */
    @DiscoverService(name = "serviceConfig", describe = "获取oqs的配置")
    public Service serviceConfigDiscover() {
        SDKAgentConfig sdkAgentConfig = systemInfoConfiguration.generateSystemInfo();
        return Service.newBuilder()
            .addAllMiddleWare(sdkAgentConfig.toMiddleWares())
            .addAllUtm(sdkAgentConfig.toUTMs())
            .addAllApplication(showApplications())
            .build();
    }

    private List<Application> showApplications() {
        return metaManager.showApplications()
                    .stream().map(
                        appInfo -> {
                            return Application.newBuilder()
                                .setId(appInfo.getAppId())
                                .setEnv(appInfo.getEnv())
                                .setCode(appInfo.getCode())
                                .setVersion(appInfo.getVersion())
                                .build();
                        }
                    )
                    .collect(Collectors.toList());
    }

}
