package com.xforceplus.ultraman.oqsengine.boot;

import kamon.Kamon;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 接收参数 -Dds={数据源配置路径},或者当前类路径下有"oqsengine-ds.conf" 文件.
 */
@SpringBootApplication
public class OqsengineBootApplication {

    public static void main(String[] args) throws Exception {

        Kamon.init();
        SpringApplication.run(OqsengineBootApplication.class, args);
    }
}
