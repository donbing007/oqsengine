package com.xforceplus.ultraman.oqsengine.common.parser.redis;

import com.xforceplus.ultraman.oqsengine.common.parser.KeyValueParser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

/**
 * RedisInfoParser Tester.
 *
 * @author <Authors name>
 * @version 1.0 11/16/2020
 * @since <pre>Nov 16, 2020</pre>
 */
public class RedisInfoParserTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: parse(String info)
     */
    @Test
    public void testParse() throws Exception {
        String info = "redis_version:6.0.9\r\n" +
            "redis_git_sha1:00000000\r\n" +
            "redis_git_dirty:0\r\n" +
            "redis_build_id:3012f034cc61d5f3\r\n" +
            "redis_mode:standalone\r\n" +
            "os:Linux 4.19.76-linuxkit x86_64\r\n" +
            "arch_bits:64\r\n" +
            "multiplexing_api:epoll\r\n" +
            "atomicvar_api:atomic-builtin\r\n" +
            "gcc_version:9.3.0\r\n" +
            "process_id:1\r\n" +
            "run_id:2d2d0ef33ff52229cec929bb708d159b5d7e9c0e\r\n" +
            "tcp_port:6379\r\n" +
            "uptime_in_seconds:4\r\n" +
            "uptime_in_days:0\r\n" +
            "hz:10\r\n" +
            "configured_hz:10\r\n" +
            "lru_clock:11662402\r\n" +
            "executable:/data/redis-server\r\n" +
            "config_file:\r\n" +
            "io_threads_active:0\r\n";

        KeyValueParser<String, Map> parser = RedisInfoParser.getInstance();
        Map<String, String> infos = parser.parse(info);
        Assert.assertEquals("6.0.9", infos.get("redis_version"));
        Assert.assertEquals("0", infos.get("io_threads_active"));
        Assert.assertEquals("2d2d0ef33ff52229cec929bb708d159b5d7e9c0e", infos.get("run_id"));
    }


} 
