package com.xforceplus.ultraman.oqsengine.storage.master;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

/**
 * @author dongbin
 * @version 0.1 2020/11/3 15:27
 * @since 1.8
 */
public abstract class AbstractMysqlTest {

    static GenericContainer mysql0;
    static GenericContainer mysql1;

    static {
        mysql0 = new GenericContainer("mysql:5.7")
            .withEnv("MYSQL_ROOT_HOST", "%")
            .withEnv("MYSQL_ROOT_PASSWORD", "root")
            .withEnv("MYSQL_DATABASE", "oqsengine")
            .withClasspathResourceMapping("master_struct.sql", "/docker-entrypoint-initdb.d/master_struct.sql", BindMode.READ_ONLY);
        mysql0.withExposedPorts(3306);
        mysql0.start();

        mysql1 = new GenericContainer("mysql:5.7")
            .withEnv("MYSQL_ROOT_HOST", "%")
            .withEnv("MYSQL_ROOT_PASSWORD", "root")
            .withEnv("MYSQL_DATABASE", "oqsengine")
            .withClasspathResourceMapping("master_struct.sql", "/docker-entrypoint-initdb.d/master_struct.sql", BindMode.READ_ONLY);
        mysql1.withExposedPorts(3306);
        mysql1.start();


        String mysql0Jdbc = String.format(
            "jdbc:p6spy:mysql://%s:%d/oqsengine?useUnicode=true&serverTimezone=GMT&useSSL=false&characterEncoding=utf8",
            mysql0.getContainerIpAddress(), mysql0.getFirstMappedPort());
        String mysql1Jdbc = String.format(
            "jdbc:p6spy:mysql://%s:%d/oqsengine?useUnicode=true&serverTimezone=GMT&useSSL=false&characterEncoding=utf8",
            mysql1.getContainerIpAddress(), mysql1.getFirstMappedPort());

        System.setProperty("MYSQL0_JDBC_URL", mysql0Jdbc);
        System.setProperty("MYSQL1_JDBC_URL", mysql1Jdbc);
    }


}
