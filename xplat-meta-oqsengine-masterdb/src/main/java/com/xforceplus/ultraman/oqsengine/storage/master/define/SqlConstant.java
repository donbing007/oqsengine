package com.xforceplus.ultraman.oqsengine.storage.master.define;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 2021/3/19 5:20 PM
 */
public class SqlConstant {
    public static final String BUILD_UNIQUE_SQL =
            "insert into %s (id, entity, unique_key) values(?,?,?)";
    public static final String REPLACE_UNIQUE_SQL =
            "update %s set entity = ?, unique_key = ? where id = ?";
    public static final String DELETE_UNIQUE_SQL =
            "DELETE FROM %s WHERE id = ?";
    public static final String SELECT_UNIQUE_SQL =
            "select id, entity, unique_key from %s where unique_key =  ? and entity = ?";
}
