package com.xforceplus.ultraman.oqsengine.idgenerator.mock;

/**
 * Created by justin.xu on 09/2021.
 *
 * @since 1.8
 */
public class IdGenerateDbScript {
    public static final String CREATE_SEGMENT =
        "CREATE TABLE `segment`\n" +
            "(\n" +
            "    `id`          bigint(20)                                                   NOT NULL AUTO_INCREMENT COMMENT '主键',\n" +
            "    `biz_type`    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '业务标签，例如可以用来标识业务序列号的的对象字段,objectCode:fieldName',\n" +
            "    `begin_id`    bigint(20)                                                   NOT NULL DEFAULT 1 COMMENT '号段起始ID',\n" +
            "    `max_id`      bigint(20)                                                   NOT NULL DEFAULT 0 COMMENT '当前号段最大ID',\n" +
            "    `step`        int(11)                                                      NOT NULL DEFAULT 1000 COMMENT '号段增加的步长',\n" +
            "    `pattern`     varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '自定义模式',\n" +
            "    `pattern_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '需要重置的模式记录上一次重置的key值',\n" +
            "    `resetable`   tinyint(4)                                                   NOT NULL DEFAULT 0 COMMENT '是否需要根据pattern_key重置编号 0:不需要 1:需要',\n" +
            "    `mode`        tinyint(4)                                                   NOT NULL DEFAULT 2 COMMENT '1：顺序递增 2: 趋势递增',\n" +
            "    `version`     bigint(20)                                                   NOT NULL DEFAULT 1 COMMENT '版本号',\n" +
            "    `create_time` timestamp(3)                                                 NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',\n" +
            "    `update_time` timestamp(3)                                                 NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',\n" +
            "    PRIMARY KEY (`id`) USING BTREE\n" +
            ") ENGINE = InnoDB\n" +
            "  AUTO_INCREMENT = 1\n" +
            "  CHARACTER SET = utf8mb4\n" +
            "  COLLATE = utf8mb4_general_ci\n" +
            "  ROW_FORMAT = Dynamic;\n";
}
