#引言

为元数据提供统一下的对象查询储存引擎

#项目结构说明

增删模块请同步.

* xplat-meta-oqsengine-boot 服务端的启动引导.
* xplat-meta-oqsengine-common 通用工具.
* xplat-meta-oqsengine-core 逻辑实现核心.
* xplat-meta-oqsengine-index 索引实现.
* xplat-meta-oqsengine-masterdb 主数据库实现.
* xplat-meta-oqsengine-pojo 相关封装对象定义.(EntityClass等)
* xplat-meta-oqsengine-cdc 数据同步相关.
* xplat-meta-oqsengine-status 状态管理.
* xplat-meta-oqsengine-sdk SDK 实现.
* xplat-meta-oqsengine-transfer 通信实现.
* xplat-meta-oqsengine-testreport 测试覆盖的聚合项目.不影响实际功能.

包名前辍统一使用 `com.xforceplus.ultraman.oqsengine`

#依赖
依赖的最小元数据版本为1.0.0.

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
    search:
      name: "oqsindex" # 搜索使用的索引名称.
      maxQueryTimeMs: 0 # 搜索使用的最大毫秒.
    write:
      name: "oqsindex" # 写入索引使用的基础名称.
      shard:
        enabled: false # 是否分表.
        size: 1 # 分表数量
```

## master 结构
查看script/mastdb.sql
```sql
create table oqsbigentity
(
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
    profile        varchar(64) default '' not null comment '替身',
    primary key (id),
    KEY commitid_entity_index (commitid, entityclassl0),
    KEY tx_index (tx),
    KEY update_time_index (updatetime)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
```
依赖sharejdbc来进行客户端分表处理.

## mainticore (Sphinx) 结构
查看script/manticore.sql
以上索引结构中,id 是默认的其和主库保持同步.即同一个 id 表示同一个实例数据.
```sql
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
```

attr 可以理解作为一个普通的属性来使用,支持范围查询.
attrf 实际是一个全文索引字段,以文本形式组织了文档.并会将不同的学段单独编码储存组成一个大的字符串.
所以对于 mainticore 的服务端必须要打开如下配置.

# 启动/关闭服务
这是一个标准的 sprint boot 实现. 最低需求 jdk8.
关闭只需要执行以下 http 请求.
`curl -X POST http://host:8086/actuator/shutdown`

# 监控
oqsengine 会在 /actuator/prometheus 公开一系列指标来输出当前系统状态.如下.

| 指标名称 | 标签 | 说明 |
| :------- | :--- | :---- |
| executor_queue_remaining_tasks | name(线程池名称) | 还可以接收的任务数量 |
| executor_pool_size_threads | name(线程池名称) | 线程池最大线程数量 |
| executor_queued_tasks | name(线程池名称 | 正在排队等待执行的任务 |
| executor_active_threads | name(线程池名称) | 当前活动的线程 |
| executor_completed_tasks_total | name(线程池名称) | 已经完成的任务数量 |
| oqs_write_count_total | action(build,delete,replace) | 写入数据总量 |
| oqs_transaction_count | | 活动事务量 |
| oqs_transaction_duration_seconds | | 事务的持续时间 |
| oqs_fail_count_total | | 错误总量 |
| oqs_read_count_total | action(one,nultiple,serch) | 读取数据总量 |
| oqs_process_delay_latency_seconds_max | initiator(all,master,index), action(one,condition) | 操作延时最大值(秒) |
| oqs_process_delay_latency_seconds_count | initiator(all,master,index) action(one,condition) | 操作延时统计数量 |
| oqs_process_delay_latency_seconds_sum | initiator(all,master,index) action(one,condition) | 操作延时总和 |
| oqs_unsync_commitid_count_total | | 提交但未同步的提交号数量 |
| oqs_cdc_sync_delay_latency | | CDC 同步的延时 |
| oqs_mode | | 当前工作模式(1 正常, 2 只读) |
| oqs_readonly_rease | | 如果进入了只读模式,此值表示原因. (取值 1 正常.<p>2 CDC心跳失败造成只读.<p>3 未提交号过多造成只读.<p>4 CDC服务离线造成只读.)|
| oqs_now_commitid | | 当前最大提交号 |

# META同步工具(Meta/Client/Server)
## Meta逻辑
Redis作为1级缓存存储EntityClass的原始信息,提供Client端SyncExecutor的实现类EntityClassSyncExecutor,进行同步新版本操作。
Redis存储逻辑:
1.prepare-锁定该AppId一分钟，如果锁定失败，则表示当前本AppId有更新操作正在执行，本次操作将被拒绝，并直接返回更新失败。否则将会获取当前AppId的版本作为将过期版本。
2.将protobuf转为EntityClassStorage列表。
3.保存当前内容
    3.1 保存基础信息
    3.2 保存Field/relation信息
    3.3 重置当前活动版本
4.将过期版本加入过期队列，一分钟后清除。   
 

## Client逻辑
client向外部提供register接口，通过register将AppId+Env注册到Server端，并在首次注册时告知server端当前活动版本，注册过的AppId将被加入client端的watchList中，
当客户端发生断开链接或断流时，client端将自动重新注册WatchList中的所有关注元素到server端(通过IRequestHandler的reRegister自动注册)。
client接受数据后调用Meta提供的SyncExecutor中的sync方法进行数据同步，并将同步结果回推到server端。
client端启动后会发送HEARTBEAT请求到server端，发送频率默认为5秒每次，当30秒内未收到server端的确认时，将进行自动断流重连.


## Server逻辑
server端需要外部提供pull接口(server主动拉取外部服务meta信息, 并实现了push接口，提供了通过EventListener的方式由外部主动推送版本发布更新信息)
server端维护以下3个内存数据结构:
map -> appVersions, key: 'app_env' value: version，每次外部主动推送一个版本，会触发该map的更新(map中的版本小于推送版本则代表可以推送),将触发推送下发的逻辑.
map -> appWatchers, key: 'app_env' value: Set<String>(uids),维护了关注列表，及关注该key的所有client uids的合集.每次client调用register事件时，将触发在该set中新增自己的uid的操作.
map -> uidWatchers, key: 'uid' value: responseWatcher，每个uid的关注列表，以及当前版本信息, responseWatcher中持有当前uid所对应的client的responseStreamObserver，关注列表中记录当前client
的所有AppId的状态，当前版本，环境(server端需要同时支持同个AppId的多套环境)，最后心跳时间戳等。
每次推送给client数据更新包后，将会将当前推送信息加入到delayTask队列中，如果client端在指定时间内未响应同步成功信息后，将触发服务端的重新发送任务。

