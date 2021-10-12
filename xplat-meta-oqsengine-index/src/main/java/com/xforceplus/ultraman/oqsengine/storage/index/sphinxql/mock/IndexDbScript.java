package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.mock;

/**
 * Created by justin.xu on 09/2021.
 *
 * @since 1.8
 */
public class IndexDbScript {
    public static final String DROP_INDEX = "drop table if exists ";

    public static final String CREATE_INDEX_0 =
        "create table if not exists oqsindex0\n" +
            "(\n" +
            "    attrf        text indexed,\n" +
            "    entityclassf text indexed,\n" +
            "    tx           bigint,\n" +
            "    commitid     bigint,\n" +
            "    createtime   bigint,\n" +
            "    updatetime   bigint,\n" +
            "    maintainid   bigint,\n" +
            "    oqsmajor     int,\n" +
            "    attr         json\n" +
            ") charset_table='non_cjk,cjk' rt_mem_limit='1024m'";

    public static final String CREATE_INDEX_1 = "create table if not exists oqsindex1 like oqsindex0";
    public static final String SEARCH_INDEX = "create table if not exists oqsindex type='distributed' local='oqsindex0' local='oqsindex1'";

}
