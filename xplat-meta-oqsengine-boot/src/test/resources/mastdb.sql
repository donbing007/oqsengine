use oqsengine;
create table oqsbigentity (
    id             bigint                not null comment '数据主键',
    entityclassl0  bigint  default 0     not null comment '数据家族中在0层的entityclass标识',
    entityclassl1  bigint  default 0     not null comment '数据家族中在1层的entityclass标识',
    entityclassl2  bigint  default 0     not null comment '数据家族中在2层的entityclass标识',
    entityclassl3  bigint  default 0     not null comment '数据家族中在3层的entityclass标识',
    entityclassl4  bigint  default 0     not null comment '数据家族中在4层的entityclass标识',
    entityclassver int     default 0     not null comment '产生数据的entityclass版本号.',
    tx             bigint  default 0     not null comment '提交事务号',
    commitid       bigint  default 0     not null comment '提交号',
    op             tinyint default 0     not null comment '最后操作类型,0(插入),1(更新),2(删除)',
    version        int     default 0     not null comment '当前数据版本.',
    createtime     bigint  default 0     not null comment '数据创建时间.',
    updatetime     bigint  default 0     not null comment '数据操作最后时间.',
    deleted        boolean default false not null comment '是否被删除.',
    attribute      json                  not null comment '当前 entity 的属性集合.',
    oqsmajor       int     default 0     not null comment '产生数据的oqs主版本号',
    primary key (id),
    KEY commitid_entity_index (entityclassl0, entityclassl1, entityclassl2, entityclassl3, entityclassl4, commitid),
    KEY tx_index (tx),
    KEY update_time_index (updatetime)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

create table cdcerrors
(
    seqno       bigint           not null comment '数据主键',
    id          bigint           not null comment '业务主键ID',
    commitid    bigint default 0 not null comment '提交号',
    status      tinyint          not null comment '处理状态，0(未处理),1(已处理),2(处理失败)',
    message     varchar(1024) comment '出错信息',
    executetime bigint           not null comment '出错时间戳',
    fixedtime   bigint           not null comment '修复时间.',
    constraint cdcerror_pk primary key (seqno)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

create TABLE changeversion (
  vid BIGINT(20) NOT NULL AUTO_INCREMENT,
  id BIGINT(20) DEFAULT NULL,
  user VARCHAR(255) DEFAULT NULL,
  comment VARCHAR(255) DEFAULT NULL,
  timestamp BIGINT(20) DEFAULT NULL,
  version BIGINT(20) DEFAULT NULL,
  count BIGINT(20) DEFAULT NULL,
  source BIGINT(20) DEFAULT NULL,
  PRIMARY KEY (vid)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

create TABLE changesnapshot (
  sid BIGINT(20) NOT NULL AUTO_INCREMENT,
  id BIGINT(20) DEFAULT NULL,
  entity BIGINT(20) DEFAULT NULL,
  changes JSON DEFAULT NULL,
  reference JSON DEFAULT NULL,
  version BIGINT(20) DEFAULT NULL,
  create_time VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (sid)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;


create TABLE changelog (
  cid BIGINT(20) NOT NULL AUTO_INCREMENT,
  id BIGINT(20) DEFAULT NULL,
  entity BIGINT(20) DEFAULT NULL,
  changes JSON DEFAULT NULL,
  reference JSON DEFAULT NULL,
  version BIGINT(20) DEFAULT NULL,
  create_time VARCHAR(255) DEFAULT NULL,
  comment VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (cid)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;