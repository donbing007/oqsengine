package com.xforceplus.ultraman.oqsengine.common.selector;

import static org.mockito.Mockito.mock;

import com.xforceplus.ultraman.oqsengine.common.hash.Time33Hash;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * DataSourceHashSelector Tester.
 *
 * @author dongbin
 * @version 1.0 02/20/2020
 * @since <pre>Feb 20, 2020</pre>
 */
public class HashSelectorTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: select(String key).
     */
    @Test
    public void testSelect() throws Exception {

        int dsSize = 10;
        List<DataSource> dsPool = buildDataSource(dsSize);
        int keySize = 200;
        List<String> keyPool = buildKeys(keySize);

        HashSelector selector = new HashSelector(dsPool);
        Time33Hash h = Time33Hash.build();
        for (String key : keyPool) {
            int address = Math.abs(h.hash(key) % dsSize);

            Assert.assertEquals(dsPool.get(address), selector.select(key));
        }

    }

    private List<String> buildKeys(int size) {
        List<String> keys = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            keys.add(randomString(10));
        }
        return keys;
    }

    private List<DataSource> buildDataSource(int size) {
        List<DataSource> ds = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            ds.add(mock(DataSource.class));
        }
        return ds;
    }

    private String randomString(int len) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

} 
