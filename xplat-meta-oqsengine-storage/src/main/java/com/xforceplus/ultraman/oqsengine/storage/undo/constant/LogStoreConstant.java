package com.xforceplus.ultraman.oqsengine.storage.undo.constant;

import org.springframework.util.StringUtils;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/23/2020 11:46 AM
 * 功能描述:
 * 修改历史:
 */
public class LogStoreConstant {

    public static final String UNDO_LOG = "OQSENGINE_UNDO_LOG";

    public static final String SEPARATOR = "-";

    public static String getLogKey(Long txId, DbTypeEnum dbType, OpTypeEnum opType){
        return txId + SEPARATOR  + dbType.name() + SEPARATOR + opType.name();
    }

    public static Long getTxIdByKey(String key){
        if(StringUtils.isEmpty(key)) {
            return null;
        }
        String[] items = key.split("-");
        if(!isNumeric(items[0])) {
            return null;
        }
        return Long.parseLong(items[0]);
    }

    public static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        } else {
            int sz = str.length();

            for(int i = 0; i < sz; ++i) {
                if (!Character.isDigit(str.charAt(i))) {
                    return false;
                }
            }

            return true;
        }
    }

//    public static void main(String[] args) {
//        System.out.println(CacheConstant.getTxIdByKey(getLogKey(2222L, DbTypeEnum.INDEX, OpTypeEnum.DELETE)));
//    }

}
