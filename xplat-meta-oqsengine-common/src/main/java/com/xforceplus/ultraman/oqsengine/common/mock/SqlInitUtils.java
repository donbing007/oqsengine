package com.xforceplus.ultraman.oqsengine.common.mock;

import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.ManticoreContainer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by justin.xu on 10/2021.
 *
 * @since 1.8
 */
public class SqlInitUtils {
    private static final Logger
        LOGGER = LoggerFactory.getLogger(ManticoreContainer.class);

    private static List<String> readSqls(String resource) throws IOException {
        File path = new File(ManticoreContainer.class.getResource(resource).getPath());
        String[] sqlFiles = path.list((dir, name) -> {
            String[] names = name.split("\\.");
            if (names.length == 2 && names[1].equals("sql")) {
                return true;
            }
            return false;
        });

        List<String> sqls = new ArrayList();
        for (String file : sqlFiles) {
            String fullPath = String.format("%s%s%s", path.getAbsolutePath(), File.separator, file);
            LOGGER.info("Reader sql file: {}", fullPath);
            try (BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(fullPath), "utf8"))) {
                String line;
                StringBuilder buff = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    if (!line.isEmpty()) {
                        buff.append(line);
                        if (buff.charAt(buff.length() - 1) == ';') {
                            buff.deleteCharAt(buff.length() - 1);
                            sqls.add(buff.toString());

                            LOGGER.info(buff.toString());

                            buff = new StringBuilder();
                        }
                    }
                }
            }
        }

        return sqls;
    }

    public static void init(String resource, DataSource dataSource) throws Exception {

        List<String> sqlList = readSqls(resource);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        try (Connection conn = dataSource.getConnection()) {
            try (Statement statement = conn.createStatement()) {
                for (String sql : sqlList) {
                    statement.execute(sql);
                }
            }
        }
    }

    public static void init(String resource, String propertyName) throws Exception {

        List<String> sqlList = readSqls(resource);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        try (Connection conn = DriverManager.getConnection(System.getProperty(propertyName))) {
            try (Statement statement = conn.createStatement()) {
                for (String sql : sqlList) {
                    statement.execute(sql);
                }
            }
        }
    }

}
