package com.xforceplus.ultraman.oqsengine.storage.undo.constant;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/23/2020 11:46 AM
 * 功能描述:
 * 修改历史:
 */
public class CacheConstant {

    public static final String UNDO_LOG = "OQSENGINE_UNDO_LOG";

    public static final String SEPARATOR = "-";

    public static String getLogKey(Long txId, DbTypeEnum dbType, OpTypeEnum opType){
        return "LogKey@" + txId + SEPARATOR  + dbType.name() + SEPARATOR + opType.name();
    }
}
