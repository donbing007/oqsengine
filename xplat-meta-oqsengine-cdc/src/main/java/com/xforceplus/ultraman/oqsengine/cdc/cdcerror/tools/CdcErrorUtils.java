package com.xforceplus.ultraman.oqsengine.cdc.cdcerror.tools;


import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.dto.ErrorType;

/**
 * Created by justin.xu on 05/2021
 */
public class CdcErrorUtils {

    public static String uniKeyGenerate(String uniKeyPrefix, int pos, ErrorType errorType) {
        return uniKeyPrefix + "-" + pos +  "-" + errorType.getType();
    }
}
