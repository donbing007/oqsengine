package com.xforceplus.ultraman.oqsengine.sdk.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.util.StringUtils;

import javax.naming.directory.NoSuchAttributeException;
import java.io.File;

/**
 * 读取application.conf中的pfcp 配置
 * @author wangzheng
 * @version 0.1 2020/2/23 16:25
 * @since 1.8
 */
public class ExternalServiceConfig {

    public static final String CONFIG_FILE = "application.conf";

    private static final String PFCP_ACCESSURI = "pfcp.accessUri";

    public static String PfcpAccessUri() throws NoSuchAttributeException {
        String appConfigFile = System.getProperty(CONFIG_FILE);
        Config config;
        if (appConfigFile == null) {
            config = ConfigFactory.load("application.conf");
        } else {
            config = ConfigFactory.parseFile(new File(appConfigFile));
        }
        String uri = "";
        if (config.hasPath(PFCP_ACCESSURI)) {
            uri = config.getString(PFCP_ACCESSURI);
            if (StringUtils.isEmpty(uri)) {
                throw new NoSuchAttributeException(
                        String.format("The '%s' property setting error in application.conf.", PFCP_ACCESSURI));
            }else {
                return uri;
            }
        }else {
            throw new NoSuchAttributeException(
                    String.format("The '%s' property setting could not be found in application.conf.", PFCP_ACCESSURI));
        }
    }

}
