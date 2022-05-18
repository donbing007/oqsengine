package com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc;

import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.OriginalFieldAgent;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.OriginalFieldAgentFactory;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * JDBC 原生字段处理代理人工厂.
 *
 * @author dongbin
 * @version 0.1 2022/3/9 10:55
 * @since 1.8
 */
public class JdbcOriginalFieldAgentFactory implements OriginalFieldAgentFactory<Integer> {

    private static JdbcOriginalFieldAgentFactory INSTANCE = new JdbcOriginalFieldAgentFactory();

    private Map<Integer, JdbcOriginalFieldAgent> agents;

    /**
     * 获取工厂实例.
     *
     * @return 实例.
     */
    public static JdbcOriginalFieldAgentFactory getInstance() {
        return INSTANCE;
    }

    /**
     * 实例化一个JDBC原生类型agent工厂.
     */
    private JdbcOriginalFieldAgentFactory() {
        agents = new HashMap<>();

        agents.put(Types.SMALLINT, new JdbcSmallintOriginalFieldAgent());
        agents.put(Types.INTEGER, new JdbcIntegerOriginalFieldAgent());
        agents.put(Types.BIGINT, new JdbcBigintOriginalFieldAgent());

        agents.put(Types.BIT, new JdbcBitOriginalFieldAgent());
        agents.put(Types.BLOB, new JdbcBlobOriginalFieldAgent());

        agents.put(Types.BOOLEAN, new JdbcBooleanOriginalFieldAgent());

        agents.put(Types.CHAR, new JdbcCharOriginalFieldAgent());
        agents.put(Types.VARCHAR, new JdbcVarcharOriginalFieldAgent());

        agents.put(Types.DECIMAL, new JdbcDecimalOriginalFieldAgent());
        agents.put(Types.DOUBLE, new JdbcDoubleOriginalFieldAgent());
        agents.put(Types.FLOAT, new JdbcFloatOriginalFieldAgent());

        agents.put(Types.DATE, new JdbcCharOriginalFieldAgent());
        agents.put(Types.TIME, new JdbcTimeOriginalFieldAgent());
        agents.put(Types.TIMESTAMP, new JdbcTimestampOriginalFieldAgent());
        agents.put(Types.TINYINT, new JdbcTinyintOriginalFieldAgent());
    }

    @Override
    public Optional<OriginalFieldAgent> getAgent(Integer type) {
        return Optional.ofNullable(agents.get(type));
    }


}
