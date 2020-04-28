package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 4/3/2020 7:07 PM
 * 功能描述:
 * 修改历史:
 */
public class CommonUtil {

    // 格式化全文属性为字符串.
    public static String toFullString(Set<String> fullFields) {
        return fullFields.stream().collect(Collectors.joining(" "));
    }

}
