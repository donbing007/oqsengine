package com.xforceplus.ultraman.oqsengine.storage.kv.sql.define;

/**
 * sql的模板定义.
 *
 * @author dongbin
 * @version 0.1 2021/07/16 10:28
 * @since 1.8
 */
public final class SqlTemplateDefine {

    private SqlTemplateDefine() {
    }

    /**
     * 查询数字.<br>
     * {1} KEY<br>
     * {2} HASH<br>
     */
    public static final String SELECT_NUMBER_TEMPLATE =
        String.format("SELECT %s FROM %s WHERE %s = ? AND %s = ? LIMIT 0, 1",
            FieldDefine.VALUE, "%s", FieldDefine.KEY, FieldDefine.HASH);

    /**
     * 更新已有数字KEY的值.<br>
     * {1} 增加的数量<br>
     * {2} KEY<br>
     * {3} hash<br>
     */
    public static final String UPDATE_NUMBER_TEMPLATE =
        String.format("UPDATE %s set %s = %s + ? WHERE %s = ? AND %s = ?",
            "%s", FieldDefine.VALUE, FieldDefine.VALUE, FieldDefine.KEY, FieldDefine.HASH);

    /**
     * 增加一个KEY-VALUE.<p></p>
     * {1}  key<br>
     * {2}  hash<br>
     * {3}  value<br>
     */
    public static final String INSERT_TEMPLATE =
        String.format("INSERT INTO %s (%s, %s, %s) values (?, ?, ?)", "%s",
            FieldDefine.KEY, FieldDefine.HASH, FieldDefine.VALUE);

    /**
     * 更新KEY-VALUE.<p></p>
     * {1}  key<br>
     * {2}  key的哈希<br>
     * {3}  value<br>
     */
    public static final String REPLACE_TEMPLATE =
        String.format("REPLACE INTO %s (%s, %s, %s) values (?, ?, ?)", "%s",
            FieldDefine.KEY, FieldDefine.HASH, FieldDefine.VALUE);

    /**
     * 删除KEY-VALUE.<p></p>
     * {1}  key.<p></p>
     * {2}  key的哈希.
     */
    public static final String DELETE_TEMPLATE = String.format("DELETE FROM %s WHERE %s = ? AND %s = ?", "%s",
        FieldDefine.KEY, FieldDefine.HASH);

    /**
     * 查询某个KEY-VALUE的VALUE.<p></p>
     * {1} 完整KEY.<p></p>
     * {2} key的hash<br>
     */
    public static final String SELECT_TEMPLATE =
        String.format("SELECT %s FROM %s WHERE %s = ? AND %s = ? limit 0, 1",
            FieldDefine.VALUE, "%s", FieldDefine.KEY, FieldDefine.HASH);

    /**
     * 批量查询KEY的VALUE<br>
     * 需要动态组织 ? .
     */
    public static final String SELECTS_TEMPLATE =
        String.format("SELECT %s,%s FROM %s WHERE %s IN (%s) AND %s IN (%s)",
            FieldDefine.KEY, FieldDefine.VALUE, "%s", FieldDefine.KEY, "%s", FieldDefine.HASH, "%s");

    /**
     * 判断指定的key是否存在.<p></p>
     * 返回结果如果有命中则返回恒定的1.<br>
     * {1}  完整的key.<br>
     * {2}  key的hash<br>
     */
    public static final String EXIST_TEMPLATE =
        String.format("SELECT 1 FROM %s WHERE %s = ? AND %s = ? LIMIT 0, 1", "%s", FieldDefine.KEY, FieldDefine.HASH);

    /**
     * 迭代key首次.<p></p>
     * {1}  开始的key.需要以'%'结尾.<p></p>
     * {2}  获取数量<p></p>
     */
    public static final String ITERATOR_FIRST_TEMPLATE =
        String.format("SELECT %s FROM %s WHERE %s like ? order by %s %s limit 0, ?",
            FieldDefine.KEY, "%s", FieldDefine.KEY, FieldDefine.KEY, "%s");

    /**
     * 迭代key非首次.<p></p>
     * {1} 开始的key.需要以'%'结尾.<p></p>
     * {2} 上次查询的结尾精确key.<p></p>
     * {3} 获取数量.<p></p>
     */
    public static final String ITERATOR_NO_FIRST_TEMPLATE =
        String.format("SELECT %s FROM %s WHERE %s like ? and %s %s ? order by %s %s limit 0, ?",
            FieldDefine.KEY, "%s", FieldDefine.KEY, FieldDefine.KEY, "%s", FieldDefine.KEY, "%s");

}
