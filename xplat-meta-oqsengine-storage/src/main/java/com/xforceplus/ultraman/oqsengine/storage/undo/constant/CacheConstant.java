package com.xforceplus.ultraman.oqsengine.storage.undo.constant;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/23/2020 11:46 AM
 * 功能描述:
 * 修改历史:
 */
public class CacheConstant {

    public static final String UNDO_LOG = "UNDO_LOG";
    public static String getTxIdKey(Long txId){
        return "txId" + txId;
    }
}
