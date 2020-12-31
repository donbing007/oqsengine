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
不过由于主库进行了分表设计,使用的是表名加上数字,从0开始.如里分表为3,那么如下需要3个物理表.
oqsbigentity0, oqsbigentity1, oqsbigentity2
storage.master.shard.enabled = true 才会启效.

## mainticore (Sphinx) 结构
查看script/manticore.sql
以上索引结构中,id 是默认的其和主库保持同步.即同一个 id 表示同一个实例数据.
* entity      实例数据的类型 id.
* pref        指向实例父类实例id.
* cref        指向实例子类实例 id.
* jsonfields  搜索的索引属性集合,是一个 JSON 格式.
* fullfields  搜索的全文索引属性集合结构如下.
              <F{groupName}>F{fieldName}{fieldType} {value}</groupName>
              
jsonFields 可以理解作为一个普通的属性来使用,支持范围查询.
fullfields 实际是一个全文索引字段,以文本形式组织了文档.并以 html 的格式将每一个字段分隔开.
所以对于 mainticore 的服务端必须要打开如下配置.
```text
index_zones = F*              # 范围的 html 标识,所有以 F 开头的都是合法的字段标签.
html_strip = 1                # 打开 html 标签索引支持.
ngram_chars = U+3000..U+2FA1F # 非英文字符范围指定.
ngram_len = 1                 # 非英文字符的分词最小单位.
```

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
| oqs_now_commitid | | 当前最大提交号 |