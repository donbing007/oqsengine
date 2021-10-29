create table oqsindex (
    attrf        text,
    entityclassf text,
    tx           bigint,
    commitid     bigint,
    createtime   bigint,
    updatetime   bigint,
    maintainid   bigint,
    oqsmajor     int,
    attr         json
) charset_table='non_cjk,cjk' rt_mem_limit='1024m';