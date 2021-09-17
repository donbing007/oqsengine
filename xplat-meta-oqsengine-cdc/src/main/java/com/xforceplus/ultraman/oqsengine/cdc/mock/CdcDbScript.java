package com.xforceplus.ultraman.oqsengine.cdc.mock;

/**
 * Created by justin.xu on 09/2021.
 *
 * @since 1.8
 */
public class CdcDbScript {
    public static final String DROP_CDC_ERRORS = "drop table cdcerrors;";
    public static final String CREATE_CDC_ERRORS =
        "create table cdcerrors\n" +
            "(\n" +
            "    seqno           bigint                      not null comment '数据主键',\n" +
            "    unikey          varchar(512)                not null comment '唯一约束',\n" +
            "    batchid         bigint                      not null comment '批次ID',\n" +
            "    id              bigint                               comment '业务主键ID',\n" +
            "    entity          bigint                               comment 'entity 的类型 id.',\n" +
            "    version         int                                  comment '当前数据版本.',\n" +
            "    op              tinyint                              comment '最后操作类型,0(插入),1(更新),2(删除)',\n" +
            "    commitid        bigint                               comment '提交号',\n" +
            "    type            tinyint                     not null comment '错误类型,1-单条数据格式错误,2-批次数据插入失败',\n" +
            "    status          tinyint                     not null comment '处理状态,1(未处理),2(已提交处理),3(处理成功),4(处理失败)',\n" +
            "    operationobject json                                 comment '当前操作的对象(已序列化)',\n" +
            "    message         varchar(1024)                        comment '出错信息',\n" +
            "    executetime     bigint                      not null comment '出错时间戳',\n" +
            "    fixedtime       bigint                      not null comment '修复时间.',\n" +
            "    constraint cdcerror_pk primary key (seqno),\n" +
            "    unique key unikey_upk (unikey),\n" +
            "    key cdcerrors_k0 (batchid)\n" +
            ") ENGINE = InnoDB\n" +
            "  DEFAULT CHARSET = utf8mb4;";
}
