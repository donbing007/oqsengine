create table oqsindex
(
    attrf        text indexed,
    entityclassf text indexed,
    tx           bigint,
    commitid     bigint,
    createtime   bigint,
    updatetime   bigint,
    maintainid   bigint,
    oqsmajor     int,
    attr         json
) charset_table='non_cjk,cjk' rt_mem_limit='1024m';

/*
以下为单机分片的配置, 以下为3分片.
多分片依次类推即可.
*/
create table oqsindex0
(
    attrf        text indexed,
    entityclassf text indexed,
    tx           bigint,
    commitid     bigint,
    createtime   bigint,
    updatetime   bigint,
    maintainid   bigint,
    oqsmajor     int,
    attr         json
) charset_table='non_cjk,cjk' rt_mem_limit='1024m';
create table oqsindex1 like oqsindex0;
create table oqsindex2 like oqsindex0;
create table oqsindex type='distributed' local='oqsindex0' local='oqsindex1' local='oqsindex2;
