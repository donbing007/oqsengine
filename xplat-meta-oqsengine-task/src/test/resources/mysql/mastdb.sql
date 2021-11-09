use oqsengine;
create table kv (
  k  varchar(255)          not null comment 'key',
  h  bigint                not null comment 'key的哈希值,主要用以分区',
  v  blob                           comment '值',
  unique key unique_key(k, h)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 PARTITION BY HASH(h) PARTITIONS 40;