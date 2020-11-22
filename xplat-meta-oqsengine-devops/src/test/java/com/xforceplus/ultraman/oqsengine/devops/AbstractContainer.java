package com.xforceplus.ultraman.oqsengine.devops;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import org.junit.Ignore;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * desc :
 * name : AbstractContainer
 *
 * @author : xujia
 * date : 2020/11/22
 * @since : 1.8
 */
@Ignore
public abstract class AbstractContainer {
    protected static GenericContainer mysql;

    protected static String cdcErrorsTableName = "cdcerrors";

    protected static DataSourcePackage dataSourcePackage;
    static {
        Network network = Network.newNetwork();
        mysql = new GenericContainer("mysql:5.7")
                .withNetwork(network)
                .withNetworkAliases("mysql")
                .withExposedPorts(3306)
                .withEnv("MYSQL_DATABASE", "oqsengine")
                .withEnv("MYSQL_ROOT_USERNAME", "root")
                .withEnv("MYSQL_ROOT_PASSWORD", "xplat")
                .withClasspathResourceMapping("mastdb.sql", "/docker-entrypoint-initdb.d/1.sql", BindMode.READ_ONLY)
                .waitingFor(Wait.forListeningPort());
        mysql.start();

        System.setProperty("MYSQL_HOST", mysql.getContainerIpAddress());
        System.setProperty("MYSQL_PORT", mysql.getFirstMappedPort().toString());

        System.setProperty(
                "MYSQL_JDBC_URL",
                String.format("jdbc:mysql://%s:%s/oqsengine?useUnicode=true&serverTimezone=GMT&useSSL=false&characterEncoding=utf8",
                        System.getProperty("MYSQL_HOST"), System.getProperty("MYSQL_PORT")));


    }

    protected static void start() {
        System.setProperty(DataSourceFactory.CONFIG_FILE, "./src/test/resources/oqsengine-ds.conf");
        dataSourcePackage = DataSourceFactory.build();
    }

    protected static void close() {
        dataSourcePackage.close();
    }

    public void clear() throws SQLException {
        DataSource ds = dataSourcePackage.getDevOps();
        Connection conn = ds.getConnection();
        Statement st = conn.createStatement();
        st.executeUpdate("truncate table " + cdcErrorsTableName);
        st.close();
        conn.close();
    }

}
