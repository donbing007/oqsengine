create table oqsindex
(
    entity     bigint,
    pref       bigint,
    cref       bigint,
    tx         bigint,
    commitid   bigint,
    time       bigint,
    maintainid bigint,
    jsonfields json,
    fullfields text indexed
) html_strip='1' index_zones='F*' infix_fields='fullfields' min_infix_len='3' charset_table='U+ff01,U+ff04,U+ff07,U+ff09,U+ff0d,U+ff0f,U+ff1c,U+ff20,U+ff3c,U+ff3e,U+ff5c,U+ff5e,U+ff0a,U+ff02,non_cjk' ngram_chars='cjk' ngram_len='1' rt_mem_limit='1024m';
