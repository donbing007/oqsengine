package com.xforceplus.ultraman.oqsengine.boot.config.system;

import com.typesafe.config.Config;
import com.xforceplus.ultraman.devops.service.custom.pojo.config.enums.UTMAgentType;
import com.xforceplus.ultraman.devops.service.custom.pojo.config.middleware.CanalTypedConfig;
import com.xforceplus.ultraman.devops.service.custom.pojo.config.middleware.ManticoreTypedConfig;
import com.xforceplus.ultraman.devops.service.custom.pojo.config.middleware.MysqlTypedConfig;
import com.xforceplus.ultraman.devops.service.custom.pojo.config.middleware.RedisTypedConfig;
import com.xforceplus.ultraman.devops.service.custom.pojo.dto.SDKAgentConfig;
import com.xforceplus.ultraman.devops.service.custom.pojo.utm.UTMAgentConfig;
import com.xforceplus.ultraman.oqsengine.boot.util.SystemInfoConfigUtils;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
@Component
public class SystemInfoConfiguration {

    //cdc
    @Value("${cdc.connect.host:}")
    private String cdcHost;

    @Value("${cdc.connect.destination:}")
    private String cdcDestination;

    //  meta
    @Value("${meta.grpc.host:}")
    private String metaHost;

    @Value("${meta.grpc.port:0}")
    private int metaPort;

    //  redis
    @Value("${redis.lettuce.uri:}")
    private String redisLettuceUrl;

    @Value("${redis.redisson.database:}")
    private int redissonDataBase;

    //  index
    @Value("${storage.index.search.name:}")
    private String indexSearchName;

    @Value("${storage.index.write.name:}")
    private String indexWriteName;

    //  master
    @Value("${storage.master.name:}")
    private String masterName;

    private String indexSimpleUri;

    private String masterSimpleUri;

    /**
     * 构造实例.
     */
    public SystemInfoConfiguration() {
        Config config = DataSourceFactory.getConfig();

        List<Config> indexConfigs =
            (List<Config>) config.getConfigList(DataSourceFactory.INDEX_WRITER_PATH);
        if (null != indexConfigs && indexConfigs.size() > 0) {
            indexSimpleUri = SystemInfoConfigUtils.getJdbcConfigUri(indexConfigs.get(0));
        }

        List<Config> masterConfigs =
            (List<Config>) config.getConfigList(DataSourceFactory.MASTER_PATH);
        if (null != masterConfigs && masterConfigs.size() > 0) {
            masterSimpleUri = SystemInfoConfigUtils.getJdbcConfigUri(masterConfigs.get(0));
        }
    }

    /**
     * 生成system-info.
     */
    public SDKAgentConfig generateSystemInfo() {
        SDKAgentConfig sdkAgentConfig = new SDKAgentConfig();
        //  cdc
        sdkAgentConfig.addService(new CanalTypedConfig(cdcHost, cdcDestination));
        sdkAgentConfig.addService(new RedisTypedConfig(SystemInfoConfigUtils.getSimpleUrl("@", "?", redisLettuceUrl)
            , Integer.toString(redissonDataBase)));
        sdkAgentConfig.addService(new ManticoreTypedConfig(indexSimpleUri, indexSearchName, indexWriteName));
        sdkAgentConfig.addService(new MysqlTypedConfig(masterSimpleUri, masterName));

        sdkAgentConfig.addAgentConfig(new UTMAgentConfig(UTMAgentType.BOCP, metaHost, metaPort));

        return sdkAgentConfig;
    }

    /**
     * 打印系统变量.
     */
    public Map<String, String> printSystemInfo() {
        Map<String, String> info = new LinkedHashMap<>();

        try {
            //  add cdc
            info.put("cdcHost", cdcHost);
            info.put("cdcDestination", cdcDestination);

            //  add meta
            info.put("metaHost", metaHost);
            info.put("metaPort", Integer.toString(metaPort));

            //  add redis
            info.put("redisLettuceUrl", SystemInfoConfigUtils.getSimpleUrl("@", "?", redisLettuceUrl));
            info.put("redissonDataBase", Integer.toString(redissonDataBase));

            //  add index
            info.put("indexSearchName", indexSearchName);
            info.put("indexWriteName", indexWriteName);
            info.put("indexSimpleUri", indexSimpleUri);

            //  add master
            info.put("masterName", masterName);
            info.put("masterSimpleUri", masterSimpleUri);

        } catch (Exception e) {
            //  log error;
        }
        return info;

    }
}
