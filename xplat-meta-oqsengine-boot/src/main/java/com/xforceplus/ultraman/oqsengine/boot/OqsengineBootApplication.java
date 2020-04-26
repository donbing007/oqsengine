package com.xforceplus.ultraman.oqsengine.boot;

import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import kamon.Kamon;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 接收参数 -Dds={数据源配置路径},或者当前类路径下有"oqsengine-ds.conf" 文件.
 * 不需要 spring 管理数据源.
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class})
public class OqsengineBootApplication {

    public static void main(String[] args) throws Exception {

        Kamon.init();
        ConfigurableApplicationContext context = SpringApplication.run(OqsengineBootApplication.class, args);

        EntitySearchService service = context.getBean(EntitySearchService.class);
        service.selectOne(100L, new EntityClass(1, "test"));
    }
}
