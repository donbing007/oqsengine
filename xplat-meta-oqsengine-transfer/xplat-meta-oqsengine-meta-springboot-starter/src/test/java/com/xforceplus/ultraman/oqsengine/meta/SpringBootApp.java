package com.xforceplus.ultraman.oqsengine.meta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * desc :
 * name : SpringBootApp
 *
 * @author : xujia
 * date : 2021/3/3
 * @since : 1.8
 */
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
//@ComponentScan({ "com.xforceplus.ultraman.oqsengine.meta"})
public class SpringBootApp {
    /**
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(SpringBootApp.class, args);
    }

}
