package com.xforceplus.ultraman.oqsengine.formula.client.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 获取当前string 的md5值.
 *
 * @author j.xu
 * @version 0.1 2021/05/2021/5/10
 * @since 1.8
 */
public class MD5Utils {

    /**
     * 获取string的md5.
     */
    public static String encrypt(String str) {
        if (str == null || str.length() == 0) {
            throw new IllegalArgumentException("String to encrypt cannot be null or zero length");
        }
        StringBuffer hexString = new StringBuffer();
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(str.getBytes(StandardCharsets.UTF_8));
            byte s[] = m.digest();

            for (int i = 0; i < s.length; i++) {
                hexString.append(Integer.toHexString((0x000000FF & s[i]) | 0xFFFFFF00).substring(6));
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
