package com.xforceplus.ultraman.oqsengine.idgenerator.common.constant;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 5/9/21 10:16 PM
 */
public class Constants {
    /**
     * 乐观锁重试次数.
     */
    public static final int RETRY = 6;

    /**
     * 预加载下个号段的百分比.
     */
    public static final int LOADING_PERCENT = 30;

    /**
     * generators 分布式容器名称.
     */
    public static final String GENERATORS = "generators";

    /**
     * 分布式缓存实例名称.
     */
    public static final String INSTANCE_NAME = "ID-GENERATOR";

    /**
     * 日期解析器名称.
     */
    public static final String DATE_PATTEN_PARSER = "data_patten_parser";

    /**
     * 数字解析器名称.
     */
    public static final String NUMBER_PATTEN_PARSER = "number_patten_parser";

}
