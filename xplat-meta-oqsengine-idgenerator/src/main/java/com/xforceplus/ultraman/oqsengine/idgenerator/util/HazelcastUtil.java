package com.xforceplus.ultraman.oqsengine.idgenerator.util;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import static com.xforceplus.ultraman.oqsengine.idgenerator.common.constant.Constants.INSTANCE_NAME;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 5/10/21 11:33 AM
 */
public class HazelcastUtil {

    private static Config config = new Config(INSTANCE_NAME);

    public static HazelcastInstance getInstance() {
        return Hazelcast.getOrCreateHazelcastInstance(config);
    }
}
