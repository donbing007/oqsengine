package com.xforceplus.ultraman.oqsengine.common.datasource.log;

import com.xforceplus.ultraman.oqsengine.common.StringUtils;
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
public class LoggerPreparedStatement extends LoggerStatement implements PreparedStatement {

    private static final Logger LOGGER = LoggerFactory.getLogger("sqlLogger");
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
        super(delegate);

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

        ((PreparedStatement) getDelegate()).setNull(parameterIndex, sqlType, typeName);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        ((PreparedStatement) getDelegate()).setNull(parameterIndex, sqlType);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        ((PreparedStatement) getDelegate()).setBoolean(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = Boolean.toString(x);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        ((PreparedStatement) getDelegate()).setByte(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = Byte.toString(x);
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        ((PreparedStatement) getDelegate()).setShort(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = Short.toString(x);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        ((PreparedStatement) getDelegate()).setInt(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = Integer.toString(x);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        ((PreparedStatement) getDelegate()).setLong(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = Long.toString(x);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        ((PreparedStatement) getDelegate()).setFloat(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = Float.toString(x);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        ((PreparedStatement) getDelegate()).setDouble(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = Double.toString(x);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        ((PreparedStatement) getDelegate()).setBigDecimal(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = x.toString();
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        ((PreparedStatement) getDelegate()).setString(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = StringUtils.encodeEscapeCharacters(x);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        ((PreparedStatement) getDelegate()).setBytes(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = Arrays.toString(x);
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        ((PreparedStatement) getDelegate()).setDate(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = x.toString();
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        ((PreparedStatement) getDelegate()).setDate(parameterIndex, x, cal);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = x.toString();
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        ((PreparedStatement) getDelegate()).setTime(parameterIndex, x, cal);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = x.toString();
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        ((PreparedStatement) getDelegate()).setTime(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = x.toString();
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        ((PreparedStatement) getDelegate()).setTimestamp(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = x.toString();
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        ((PreparedStatement) getDelegate()).setTimestamp(parameterIndex, x, cal);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = x.toString();
    }

    @Override
    @Deprecated
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        ((PreparedStatement) getDelegate()).setUnicodeStream(parameterIndex, x, length);
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        ((PreparedStatement) getDelegate()).setRef(parameterIndex, x);
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        ((PreparedStatement) getDelegate()).setArray(parameterIndex, x);
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        ((PreparedStatement) getDelegate()).setURL(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = x.toString();
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        ((PreparedStatement) getDelegate()).setRowId(parameterIndex, x);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = x.toString();
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        ((PreparedStatement) getDelegate()).setNString(parameterIndex, value);

        buildParameterBuffNotExist();
        this.parameter[parameterIndex - 1] = value;
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        ((PreparedStatement) getDelegate()).setNCharacterStream(parameterIndex, value, length);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        ((PreparedStatement) getDelegate()).setNCharacterStream(parameterIndex, value);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        ((PreparedStatement) getDelegate()).setNClob(parameterIndex, reader);
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        ((PreparedStatement) getDelegate()).setNClob(parameterIndex, value);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        ((PreparedStatement) getDelegate()).setNClob(parameterIndex, reader, length);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        ((PreparedStatement) getDelegate()).setSQLXML(parameterIndex, xmlObject);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        ((PreparedStatement) getDelegate()).setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        ((PreparedStatement) getDelegate()).setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        ((PreparedStatement) getDelegate()).setAsciiStream(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        ((PreparedStatement) getDelegate()).setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        ((PreparedStatement) getDelegate()).setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        ((PreparedStatement) getDelegate()).setBinaryStream(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        ((PreparedStatement) getDelegate()).setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        ((PreparedStatement) getDelegate()).setCharacterStream(parameterIndex, reader);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        ((PreparedStatement) getDelegate()).setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        ((PreparedStatement) getDelegate()).setClob(parameterIndex, x);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        ((PreparedStatement) getDelegate()).setClob(parameterIndex, reader);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        ((PreparedStatement) getDelegate()).setClob(parameterIndex, reader, length);
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        ((PreparedStatement) getDelegate()).setBlob(parameterIndex, x);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        ((PreparedStatement) getDelegate()).setBlob(parameterIndex, inputStream, length);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        ((PreparedStatement) getDelegate()).setBlob(parameterIndex, inputStream);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        ((PreparedStatement) getDelegate()).setObject(parameterIndex, x, targetSqlType);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        ((PreparedStatement) getDelegate()).setObject(parameterIndex, x);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        ((PreparedStatement) getDelegate()).setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        ((PreparedStatement) getDelegate()).setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType) throws SQLException {
        ((PreparedStatement) getDelegate()).setObject(parameterIndex, x, targetSqlType);
    }

    @Override
    public void close() throws SQLException {
        ((PreparedStatement) getDelegate()).close();
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return ((PreparedStatement) getDelegate()).getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        ((PreparedStatement) getDelegate()).setMaxFieldSize(max);
    }

    @Override
    public int getMaxRows() throws SQLException {
        return ((PreparedStatement) getDelegate()).getMaxRows();
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        ((PreparedStatement) getDelegate()).setMaxRows(max);
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return ((PreparedStatement) getDelegate()).getMetaData();
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return ((PreparedStatement) getDelegate()).getParameterMetaData();
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        ((PreparedStatement) getDelegate()).setEscapeProcessing(enable);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return ((PreparedStatement) getDelegate()).getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        ((PreparedStatement) getDelegate()).setQueryTimeout(seconds);
    }

    @Override
    public void clearParameters() throws SQLException {
        ((PreparedStatement) getDelegate()).clearParameters();

        this.parameter = null;
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        ((PreparedStatement) getDelegate()).addBatch(sql);
    }

    @Override
    public void addBatch() throws SQLException {
        ((PreparedStatement) getDelegate()).addBatch();

        if (this.parameters == null) {
            this.parameters = new ArrayList<>();
        }

        this.parameters.add(this.parameter);
        this.parameter = null;
    }

    @Override
    public void cancel() throws SQLException {
        ((PreparedStatement) getDelegate()).cancel();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return ((PreparedStatement) getDelegate()).getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        ((PreparedStatement) getDelegate()).clearWarnings();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        ((PreparedStatement) getDelegate()).setCursorName(name);
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return ((PreparedStatement) getDelegate()).getResultSet();
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return ((PreparedStatement) getDelegate()).getUpdateCount();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return ((PreparedStatement) getDelegate()).getMoreResults();
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return ((PreparedStatement) getDelegate()).getMoreResults(current);
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        ((PreparedStatement) getDelegate()).setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return ((PreparedStatement) getDelegate()).getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        ((PreparedStatement) getDelegate()).setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return ((PreparedStatement) getDelegate()).getFetchSize();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return ((PreparedStatement) getDelegate()).getResultSetConcurrency();
    }

    @Override
    public int getResultSetType() throws SQLException {
        return ((PreparedStatement) getDelegate()).getResultSetType();
    }

    @Override
    public void clearBatch() throws SQLException {
        this.parameters = null;

        ((PreparedStatement) getDelegate()).clearBatch();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return ((PreparedStatement) getDelegate()).getConnection();
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return ((PreparedStatement) getDelegate()).getGeneratedKeys();
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return ((PreparedStatement) getDelegate()).getResultSetHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return ((PreparedStatement) getDelegate()).isClosed();
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        ((PreparedStatement) getDelegate()).setPoolable(poolable);
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return ((PreparedStatement) getDelegate()).isPoolable();
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        ((PreparedStatement) getDelegate()).closeOnCompletion();
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return ((PreparedStatement) getDelegate()).isCloseOnCompletion();
    }

    @Override
    public long getLargeUpdateCount() throws SQLException {
        return ((PreparedStatement) getDelegate()).getLargeUpdateCount();
    }

    @Override
    public void setLargeMaxRows(long max) throws SQLException {
        ((PreparedStatement) getDelegate()).setLargeMaxRows(max);
    }

    @Override
    public long getLargeMaxRows() throws SQLException {
        return ((PreparedStatement) getDelegate()).getLargeMaxRows();
    }

    @Override
    public int[] executeBatch() throws SQLException {

        logBatch();

        return ((PreparedStatement) getDelegate()).executeBatch();
    }

    @Override
    public ResultSet executeQuery() throws SQLException {

        log();

        return ((PreparedStatement) getDelegate()).executeQuery();
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(sql);
        }

        return ((PreparedStatement) getDelegate()).executeQuery(sql);
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(sql);
        }

        return ((PreparedStatement) getDelegate()).executeUpdate(sql);
    }

    @Override
    public int executeUpdate() throws SQLException {

        log();

        return ((PreparedStatement) getDelegate()).executeUpdate();
    }

    @Override
    public boolean execute() throws SQLException {

        log();

        return ((PreparedStatement) getDelegate()).execute();
    }

    @Override
    public long[] executeLargeBatch() throws SQLException {
        logBatch();

        return ((PreparedStatement) getDelegate()).executeLargeBatch();
    }

    @Override
    public long executeLargeUpdate() throws SQLException {
        log();

        return ((PreparedStatement) getDelegate()).executeLargeUpdate();
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
