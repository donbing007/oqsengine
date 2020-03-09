package com.xforceplus.ultraman.oqsengine.common.datasource;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * {
 * 	"dataSources": {
 * 		"index": {
 * 			"write": [
 *                {
 * 					"driverClassName": "com.mysql.jdbc.Driver",
 * 					"url": "jdbc:mysql://localhost:3306/oqsengine",
 * 					"username": "root",
 * 					"password": "root"
 *                }
 * 			],
 * 			"search": [
 *                {
 * 					"driverClassName": "com.mysql.jdbc.Driver",
 * 					"url": "jdbc:mysql://localhost:3306/oqsengine",
 * 					"username": "root",
 * 					"password": "root"
 *                }
 * 			]
 * 		},
 * 		"master": [
 * 	        {
 * 				"driverClassName": "com.mysql.jdbc.Driver",
 * 				"url": "jdbc:mysql://localhost:3306/oqsengine",
 * 				"password": "root",
 * 				"username": "root"
 * 			}
 *     	]
 * 	}
 * }
 * 目标是读取上述目标配置文件.
 * 优先使用 -Dds 参数指定的路径文件,否则查找当前类路径下的 "oqsengine-ds.conf" 文件.
 *
 * @author dongbin
 * @version 0.1 2020/2/23 16:25
 * @since 1.8
 */
public class DataSourceFactory {

    public static final String CONFIG_FILE = "ds";

    private static final String INDEX_WRITER_PATH = "dataSources.index.write";
    private static final String INDEX_SEARCH_PATH = "dataSources.index.search";
    private static final String MASTER_PATH = "dataSources.master";

    /**
     * 数据源构造,会试图读取构造三个数据源列表.
     * 索引读,索引写和主库.
     *
     * @return 构造的数据源包装.
     */
    public static DataSourcePackage build() {
        String dsConfigFile = System.getProperty(CONFIG_FILE);

        Config config;
        if (dsConfigFile == null) {
            config = ConfigFactory.load("oqsengine-ds.conf");
        } else {
            config = ConfigFactory.parseFile(new File(dsConfigFile));
        }

        List<DataSource> indexWrite;
        if (config.hasPath(INDEX_WRITER_PATH)) {
            indexWrite = buildDataSources("indexWrite",
                (List<Config>) config.getConfigList(INDEX_WRITER_PATH));
        } else {
            indexWrite = Collections.emptyList();
        }

        List<DataSource> indexSearch;
        if (config.hasPath(INDEX_SEARCH_PATH)) {
            indexSearch = buildDataSources("indexSearch",
                (List<Config>) config.getConfigList(INDEX_SEARCH_PATH));
        } else {
            indexSearch = Collections.emptyList();
        }

        List<DataSource> master;
        if (config.hasPath(MASTER_PATH)) {
            master = buildDataSources("master",
                (List<Config>) config.getConfigList(MASTER_PATH));
        } else {
            master = Collections.emptyList();
        }

        return new DataSourcePackage(master, indexWrite, indexSearch);
    }

    private static List<DataSource> buildDataSources(String baseName, List<Config> configs) {
        List<DataSource> ds = new ArrayList<>(configs.size());
        for (int i = 0; i < configs.size(); i++) {
            ds.add(buildDataSource(baseName + "-" + i, configs.get(i)));
        }
        return ds;
    }

    private static DataSource buildDataSource(String name, Config config) {
        HikariConfig hikariConfig = new HikariConfig();

        config.entrySet().stream().forEach(e -> {

            try {
                invokeMethod(hikariConfig, e.getKey(), e.getValue());
            } catch (Exception ex) {
                throw new RuntimeException(String.format("Configuration error, wrong property '%s'.", e.getKey()));
            }
        });

        hikariConfig.setPoolName(name);

        return new HikariDataSource(hikariConfig);
    }

    private static void invokeMethod(HikariConfig hikariConfig, String attrName, ConfigValue value) throws Exception {
        Class clazz = hikariConfig.getClass();
        String methodName = "set" + attrName.toUpperCase().substring(0,1) + attrName.substring(1, attrName.length());
        Method method = null;
        switch (value.valueType()) {
            case NUMBER: {
                try {
                    method = clazz.getMethod(methodName, Long.TYPE);
                } catch (NoSuchMethodException ex) {
                    method = clazz.getMethod(methodName, Integer.TYPE);
                }
                break;
            }
            case STRING: {
                method = clazz.getMethod(methodName, String.class);
                break;
            }

            case BOOLEAN: {
                method = clazz.getMethod(methodName, Boolean.TYPE);
                break;
            }
            default: {
                throw new NoSuchMethodException(
                    String.format("The '%s' property setting could not be found.", attrName));
            }
        }

        method.invoke(hikariConfig, value.unwrapped());
    }

}