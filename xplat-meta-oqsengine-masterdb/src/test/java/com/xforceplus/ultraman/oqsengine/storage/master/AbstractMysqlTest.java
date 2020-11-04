package com.xforceplus.ultraman.oqsengine.storage.master;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

/**
 * @author dongbin
 * @version 0.1 2020/11/3 15:27
 * @since 1.8
 */
public abstract class AbstractMysqlTest {

    static GenericContainer mysql;

    static {
        mysql = new GenericContainer("mysql:5.7")
            .withEnv("MYSQL_ROOT_HOST", "%")
            .withEnv("MYSQL_ROOT_PASSWORD", "root")
            .withEnv("MYSQL_DATABASE", "oqsengine")
            .withClasspathResourceMapping("master_struct.sql", "/docker-entrypoint-initdb.d/master_struct.sql", BindMode.READ_ONLY);
        mysql.withExposedPorts(3306);
        mysql.start();


        String mysqlJdbc = String.format(
            "jdbc:mysql://%s:%d/oqsengine?useUnicode=true&serverTimezone=GMT&useSSL=false&characterEncoding=utf8",
            mysql.getContainerIpAddress(), mysql.getFirstMappedPort());

        System.setProperty("MYSQL_JDBC_URL", mysqlJdbc);
    }


}
