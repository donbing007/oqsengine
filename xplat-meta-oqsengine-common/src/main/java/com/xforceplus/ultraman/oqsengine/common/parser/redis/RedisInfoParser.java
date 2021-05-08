package com.xforceplus.ultraman.oqsengine.common.parser.redis;

import com.xforceplus.ultraman.oqsengine.common.parser.KeyValueParser;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * 解析redis的info返回字符串.
 * 其基于 "https://redis.io/commands/info" 的定义进行解析.
 *
 * @author dongbin
 * @version 0.1 2020/11/16 12:01
 * @since 1.8
 */
public class RedisInfoParser implements KeyValueParser<String, Map> {

    static final char SEGMENTATION_SYMBOLS = ':';

    public static KeyValueParser<String, Map> instance = new RedisInfoParser();

    public static KeyValueParser<String, Map> getInstance() {
        return instance;
    }

    private RedisInfoParser() {
    }

    @Override
    public Map parse(String info) {
        BufferedReader br = new BufferedReader(
            new InputStreamReader(new ByteArrayInputStream(
                info.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));

        Map infos = new HashMap();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                if (line != null && !line.isEmpty()) {

                    //忽略掉注解
                    if (line.startsWith("#")) {
                        continue;
                    }

                    int symbolsIndex = line.indexOf(SEGMENTATION_SYMBOLS);
                    infos.put(line.substring(0, symbolsIndex), line.substring(symbolsIndex + 1));
                }
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }

        return infos;
    }
}
