#引言

为元数据提供统一下的对象查询储存引擎.

#项目结构说明

增删模块请同步.

* xplat-meta-oqsengine-boot 服务端的启动引导.
* xplat-meta-oqsengine-common 通用工具.
* xplat-meta-oqsengine-core 逻辑实现核心.
* xplat-meta-oqsengine-index 索引实现.
* xplat-meta-oqsengine-masterdb 主数据库实现.
* xplat-meta-oqsengine-pojo 相关封装对象定义.(EntityClass等)
* xplat-meta-oqsengine-sdk SDK 实现.
* xplat-meta-oqsengine-transfer 通信实现.

包名前辍统一使用 `com.xforceplus.ultraman.oqsengine`

#启动配置
oqsengine 没有使用 spring 提供的数据源配置,而是自己进行了管理.
可以在启动参数中增加 -Dds={路径}指定配置文件,或者当前类路径下的"oqsengine-ds.conf"文件.

配置样例如下.使用了[HikariCP](https://github.com/brettwooldridge/HikariCP)作为数据源的实现.支持HikariCP相关的属性配置.
```json
 {
   "dataSources": {
     "index": {
       "write": [
         {
           "driverClassName": "com.mysql.jdbc.Driver",
           "jdbcUrl": "jdbc:mysql://localhost:3306/oqsengine",
           "username": "root",
           "password": "root",
           "minimumIdle": 2,
           "maximumPoolSize": 10,
           "transactionIsolation": "TRANSACTION_READ_COMMITTED"
         }
       ],
       "search": [
         {
           "driverClassName": "com.mysql.jdbc.Driver",
           "jdbcUrl": "jdbc:mysql://localhost:3306/oqsengine",
           "username": "root",
           "password": "root"
         }
       ]
     },
     "master": [
       {
         "driverClassName": "com.mysql.jdbc.Driver",
         "jdbcUrl": "jdbc:mysql://localhost:3306/oqsengine",
         "password": "root",
         "username": "root"
       }
     ]
   }
 }
```
同样项目是一个spring boot 项目,需要配置以下几个在 application.yaml 中的配置.
```yaml
storage:
  master:
    name: "oqsbigentity" # 主库的名称,可以理解为逻辑表名.
    query:
      worker: 3 # 查询的时候多线程时的最大线程数量,默认为 CPU 核数.
      timeout: 3000 # 查询超时时间,单位为毫秒.默认为3秒.
    shard:
      enabled: false # 是否表分区.
      size: 1 # 逻辑表分片数量,默认为1.
  index:
    name: "oqsindex" # 索引库名称,和主库作用相同.
```

## master 结构
```sql
create table oqsbigentity
(
	id bigint not null comment '数据主键',
	entity bigint default 0 not null comment 'entity 的类型 id.',
	version int default 0 not null comment '当前数据版本.',
	time bigint default 0 not null comment '数据操作最后时间.',
	pref bigint default 0 not null comment '指向当前类型继承的父类型数据实例id.',
	cref bigint default 0 not null comment '当前父类数据实例指向子类数据实例的 id.',
	deleted boolean default false not null comment '是否被删除.',
	attribute json not null comment '当前 entity 的属性集合.',
	constraint oqsengine_pk primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```
不过由于主库进行了分表设计,使用的是表名加上数字,从0开始.如里分表为3,那么如下需要3个物理表.
oqsbigentity0, oqsbigentity1, oqsbigentity2
storage.master.shard.enabled = true 才会启效.

## mainticore (Sphinx) 结构
索引的结构需要预先在配置文件中指定.如下.
```text
index oqsindex
{
        type = rt
        path = /var/lib/manticore/data/oqsindex
        rt_attr_bigint = entity
        rt_attr_bigint = pref
        rt_attr_bigint = cref
        rt_attr_json = jsonfields
        rt_field = fullfields

        rt_mem_limit = 1024m
        min_infix_len = 3
}
```
以上索引结构中,id 是默认的其和主库保持同步.即同一个 id 表示同一个实例数据.
* entity      实例数据的类型 id.
* pref        指向实例父类实例id.
* cref        指向实例子类实例 id.
* jsonfields  搜索的索引属性集合,是一个 JSON 格式.
* fullfields  搜索的全文索引属性集合,是一个以 F{fieldID}{fieldType} {fieldValue | unicode} 组成并以空格分隔的字符串.

# 启动/关闭服务
这是一个标准的 sprint boot 实现. 最低需求 jdk8.
关闭只需要执行以下 http 请求.
`curl -X POST http://host:8086/actuator/shutdown`

# 监控
oqsengine 会在 /actuator/prometheus 公开一系列指标来输出当前系统状态.如下.

* oqsengine.insert.number.second 每秒创建的 entity 数量.
* oqsengine.replace.number.second 每秒更新的 entity 数量.
* oqsengine.delete.number.second 每秒更新的数量.
* oqsengine.select.number.second 每秒查询数量.
* oqsengine.query.avg.second 平均查询时间.
* oqsengine.update.avg.second 平均更新时间.