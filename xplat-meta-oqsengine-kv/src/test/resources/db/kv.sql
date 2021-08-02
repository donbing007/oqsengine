create database oqsengine;
use oqsengine;
create table kv (
  k varchar(255) not null,
  v blob,
  primary key (k)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 PARTITION BY KEY() PARTITIONS 40;