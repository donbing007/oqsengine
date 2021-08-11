create database oqsengine;
use oqsengine;
create table kv (
  k     varchar(255)          not null comment 'key',
  v     blob                           comment '值',
  primary key (k)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 PARTITION BY KEY() PARTITIONS 40;