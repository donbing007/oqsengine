package com.xforceplus.ultraman.oqsengine.meta.common.utils;

import java.security.MessageDigest;

/**
 * desc :
 * name : MD5Utils
 *
 * @author : xujia
 * date : 2021/2/3
 * @since : 1.8
 */
public class MD5Utils {

    public static String getMD5(byte[] bytes) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(bytes);
            byte s[] = m.digest();
            String result = "";
            for (int i = 0; i < s.length; i++) {
                result += Integer.toHexString((0x000000FF & s[i]) | 0xFFFFFF00).substring(6);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}