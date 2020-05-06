package com.xforceplus.ultraman.oqsengine.boot;

import kamon.Kamon;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * 接收参数 -Dds={数据源配置路径},或者当前类路径下有"oqsengine-ds.conf" 文件.
 * 不需要 spring 管理数据源.
 *
 * 由于不需要 spring 管理 DataSource,所以这里排除了相关自动配置.
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    RedissonAutoConfiguration.class})
public class OqsengineBootApplication {

    public static void main(String[] args) throws Exception {

        Kamon.init();
        SpringApplication.run(OqsengineBootApplication.class, args);

    }
}
