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
) min_infix_len='3' charset_table='non_cjk' ngram_chars='cjk' ngram_len='1' rt_mem_limit='1024m';
