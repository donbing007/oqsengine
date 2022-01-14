package com.xforceplus.ultraman.oqsengine.common.datasource.log;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 对于执行的SQL进行日志打印.
 *
 * @author dongbin
 * @version 0.1 2021/11/29 12:11
 * @since 1.8
 */
public class LoggerPreparedStatement implements PreparedStatement {

    private static final Logger LOGGER = LoggerFactory.getLogger("sqlLogger");
    private PreparedStatement delegate;
    private String sql;
    private int parameterLen = 0;
    private String[] parameter;
    private List<String[]> parameters;

    /**
     * 构造新的可记录的PreparedStatement实例.
     *
     * @param delegate 目标.
     * @param sql      预编译的SQL.
     */
    public LoggerPreparedStatement(PreparedStatement delegate, String sql) {
        this.delegate = delegate;

        // 判断参数的长度,并且将?号替换成{}.
        int len = sql.length();
        StringBuilder buff = new StringBuilder();
        char c;
        for (int i = 0; i < len; i++) {
            c = sql.charAt(i);
            if ('?' == c) {
                parameterLen++;
                buff.append("{}");
            } else {
                buff.append(c);
            }
        }
        this.sql = buff.toString();
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {

        delegate.setNull(parameterIndex, sqlType, typeName);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        delegate.setNull(parameterIndex, sqlType);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        delegate.setBoolean(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = Boolean.toString(x);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        delegate.setByte(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = Byte.toString(x);
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        delegate.setShort(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = Short.toString(x);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        delegate.setInt(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = Integer.toString(x);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        delegate.setLong(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = Long.toString(x);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        delegate.setFloat(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = Float.toString(x);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        delegate.setDouble(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = Double.toString(x);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        delegate.setBigDecimal(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = x.toString();
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        delegate.setString(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = x;
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        delegate.setBytes(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = Arrays.toString(x);
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        delegate.setDate(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = x.toString();
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        delegate.setDate(parameterIndex, x, cal);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = x.toString();
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        delegate.setTime(parameterIndex, x, cal);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = x.toString();
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        delegate.setTime(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = x.toString();
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        delegate.setTimestamp(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = x.toString();
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        delegate.setTimestamp(parameterIndex, x, cal);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = x.toString();
    }

    @Override
    @Deprecated
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        delegate.setUnicodeStream(parameterIndex, x, length);
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        delegate.setRef(parameterIndex, x);
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        delegate.setArray(parameterIndex, x);
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        delegate.setURL(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = x.toString();
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        delegate.setRowId(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = x.toString();
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        delegate.setNString(parameterIndex, value);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = value;
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        delegate.setNCharacterStream(parameterIndex, value, length);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        delegate.setNCharacterStream(parameterIndex, value);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        delegate.setNClob(parameterIndex, reader);
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        delegate.setNClob(parameterIndex, value);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        delegate.setNClob(parameterIndex, reader, length);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        delegate.setSQLXML(parameterIndex, xmlObject);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        delegate.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        delegate.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        delegate.setAsciiStream(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        delegate.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        delegate.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        delegate.setBinaryStream(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        delegate.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        delegate.setCharacterStream(parameterIndex, reader);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        delegate.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        delegate.setClob(parameterIndex, x);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        delegate.setClob(parameterIndex, reader);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        delegate.setClob(parameterIndex, reader, length);
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        delegate.setBlob(parameterIndex, x);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        delegate.setBlob(parameterIndex, inputStream, length);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        delegate.setBlob(parameterIndex, inputStream);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        delegate.setObject(parameterIndex, x, targetSqlType);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        delegate.setObject(parameterIndex, x);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        delegate.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        delegate.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType) throws SQLException {
        delegate.setObject(parameterIndex, x, targetSqlType);
    }

    @Override
    public void close() throws SQLException {
        delegate.close();
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return delegate.getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        delegate.setMaxFieldSize(max);
    }

    @Override
    public int getMaxRows() throws SQLException {
        return delegate.getMaxRows();
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        delegate.setMaxRows(max);
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return delegate.getMetaData();
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return delegate.getParameterMetaData();
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        delegate.setEscapeProcessing(enable);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return delegate.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        delegate.setQueryTimeout(seconds);
    }

    @Override
    public void clearParameters() throws SQLException {
        delegate.clearParameters();

        this.parameter = null;
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        delegate.addBatch(sql);
    }

    @Override
    public void addBatch() throws SQLException {
        delegate.addBatch();

        if (this.parameters == null) {
            this.parameters = new ArrayList<>();
        }

        this.parameters.add(this.parameter);
        this.parameter = null;
    }

    @Override
    public void cancel() throws SQLException {
        delegate.cancel();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        delegate.clearWarnings();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        delegate.setCursorName(name);
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return delegate.getResultSet();
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return delegate.getUpdateCount();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return delegate.getMoreResults();
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return delegate.getMoreResults(current);
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        delegate.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return delegate.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        delegate.setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return delegate.getFetchSize();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return delegate.getResultSetConcurrency();
    }

    @Override
    public int getResultSetType() throws SQLException {
        return delegate.getResultSetType();
    }

    @Override
    public void clearBatch() throws SQLException {
        this.parameters = null;

        delegate.clearBatch();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return delegate.getConnection();
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return delegate.getGeneratedKeys();
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return delegate.getResultSetHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return delegate.isClosed();
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        delegate.setPoolable(poolable);
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return delegate.isPoolable();
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        delegate.closeOnCompletion();
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return delegate.isCloseOnCompletion();
    }

    @Override
    public long getLargeUpdateCount() throws SQLException {
        return delegate.getLargeUpdateCount();
    }

    @Override
    public void setLargeMaxRows(long max) throws SQLException {
        delegate.setLargeMaxRows(max);
    }

    @Override
    public long getLargeMaxRows() throws SQLException {
        return delegate.getLargeMaxRows();
    }

    @Override
    public int[] executeBatch() throws SQLException {

        logBatch();

        return delegate.executeBatch();
    }

    @Override
    public ResultSet executeQuery() throws SQLException {

        log();

        return delegate.executeQuery();
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(sql);
        }

        return delegate.executeQuery(sql);
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(sql);
        }

        return delegate.executeUpdate(sql);
    }

    @Override
    public int executeUpdate() throws SQLException {

        log();

        return delegate.executeUpdate();
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(sql);
        }

        return delegate.executeUpdate(sql, autoGeneratedKeys);
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(sql);
        }

        return delegate.executeUpdate(sql, columnIndexes);
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(sql);
        }

        return delegate.executeUpdate(sql, columnNames);
    }

    @Override
    public boolean execute() throws SQLException {

        log();

        return delegate.execute();
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(sql);
        }

        return delegate.execute(sql);
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(sql);
        }

        return delegate.execute(sql, autoGeneratedKeys);
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(sql);
        }

        return delegate.execute(sql, columnIndexes);
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(sql);
        }

        return delegate.execute(sql, columnNames);
    }

    @Override
    public long[] executeLargeBatch() throws SQLException {
        logBatch();

        return delegate.executeLargeBatch();
    }

    @Override
    public long executeLargeUpdate() throws SQLException {
        log();

        return delegate.executeLargeUpdate();
    }

    @Override
    public long executeLargeUpdate(String sql) throws SQLException {

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(sql);
        }

        return delegate.executeLargeUpdate(sql);
    }

    @Override
    public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(sql);
        }

        return delegate.executeLargeUpdate(sql, autoGeneratedKeys);
    }

    @Override
    public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(sql);
        }

        return delegate.executeLargeUpdate(sql, columnIndexes);
    }

    @Override
    public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(sql);
        }

        return delegate.executeLargeUpdate(sql, columnNames);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }

    /**
     * 如果参数buff不存在则创建.
     */
    private void buildParameterBuffNotExist() {
        if (parameter == null) {
            this.parameter = new String[this.parameterLen];
        }
    }

    private void log() {

        if (this.parameter != null) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(this.sql, this.parameter);
            }

            this.parameter = null;
        }
    }

    private void logBatch() {
        if (this.parameters != null) {
            if (LOGGER.isInfoEnabled()) {
                for (String[] p : this.parameters) {
                    LOGGER.info(this.sql, p);
                }
            }

            this.parameters = null;
            this.parameter = null;
        }
    }
}