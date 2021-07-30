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
     * 更新KEY-VALUE.<p></p>
     * {1}  key<p></p>
     * {2}  value<p></p>
     */
    public static final String REPLACE_TEMPLATE =
        String.format("REPLACE INTO %s (%s, %s) values (?, ?)", "%s", FieldDefine.KEY, FieldDefine.VALUE);

    /**
     * 删除KEY-VALUE.<p></p>
     * {1}  key.<p></p>
     */
    public static final String DELETE_TEMPLATE = String.format("DELETE FROM %s WHERE %s = ?", "%s", FieldDefine.KEY);

    /**
     * 查询某个KEY-VALUE的VALUE.<p></p>
     * {1} 完整KEY.<p></p>
     */
    public static final String SELECT_TEMPLATE =
        String.format("SELECT %s FROM %s WHERE %s = ? limit 0, 1", FieldDefine.VALUE, "%s", FieldDefine.KEY);

    /**
     * 判断指定的key是否存在.<p></p>
     * 返回结果如果有命中则返回恒定的1.
     * {1}  完整的key.<p></p>
     */
    public static final String EXIST_TEMPLATE =
        String.format("SELECT 1 FROM %s WHERE %s = ? limit 0, 1", "%s", FieldDefine.KEY);

    /**
     * 迭代key首次.<p></p>
     * {1}  开始的key.需要以'%'结尾.<p></p>
     * {2}  获取数量<p></p>
     */
    public static final String ITERATOR_FIRST_TEMPLATE =
        String.format("SELECT %s FROM %s WHERE %s like ? order by %s asc limit 0, ?",
            FieldDefine.KEY, "%s", FieldDefine.KEY, FieldDefine.KEY);

    /**
     * 迭代key非首次.<p></p>
     * {1} 开始的key.需要以'%'结尾.<p></p>
     * {2} 上次查询的结尾精确key.<p></p>
     * {3} 获取数量.<p></p>
     */
    public static final String ITERATOR_NO_FIRST_TEMPLATE =
        String.format("SELECT %s FROM %s WHERE %s like ? and %s > ? order by %s asc limit 0, ?",
            FieldDefine.KEY, "%s", FieldDefine.KEY, FieldDefine.KEY, FieldDefine.KEY);

}
