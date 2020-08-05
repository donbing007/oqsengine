CREATE DATABASE oqsengine;
USE oqsengine;

DROP TABLE IF EXISTS `oqsbigentity0`;
CREATE TABLE oqsbigentity0 (
  id BIGINT(20) NOT NULL COMMENT '数据主键',
  entity BIGINT(20) NOT NULL DEFAULT 0 COMMENT 'entity 的类型 id.',
  version INT(11) NOT NULL DEFAULT 0 COMMENT '当前数据版本.',
  time BIGINT(20) NOT NULL DEFAULT 0 COMMENT '数据操作最后时间.',
  pref BIGINT(20) NOT NULL DEFAULT 0 COMMENT '指向当前类型继承的父类型数据实例id.',
  cref BIGINT(20) NOT NULL DEFAULT 0 COMMENT '当前父类数据实例指向子类数据实例的 id.',
  deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否被删除.',
  attribute LONGTEXT NOT NULL COMMENT '当前 entity 的属性集合.',
  PRIMARY KEY (id)
)
ENGINE = INNODB,
AVG_ROW_LENGTH = 643,
CHARACTER SET utf8,
COLLATE utf8_general_ci;