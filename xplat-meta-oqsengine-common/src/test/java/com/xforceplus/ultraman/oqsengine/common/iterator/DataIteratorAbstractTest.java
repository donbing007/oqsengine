package com.xforceplus.ultraman.oqsengine.common.iterator;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 数据迭代器的抽像类测试.
 *
 * @author dongbin
 * @version 0.1 2021/11/24 15:43
 * @since 1.8
 */
public class DataIteratorAbstractTest {

    @Test
    public void testIterator() throws Exception {
        List<String> data = IntStream.range(0, 10).mapToObj(i -> Integer.toString(i)).collect(Collectors.toList());
        TestDataIterator iterator = new TestDataIterator(5, Long.MAX_VALUE, data);

        int size = 0;
        while (iterator.hasNext()) {
            iterator.next();
            size++;
        }

        Assertions.assertEquals(data.size(), size);
        Assertions.assertFalse(iterator.more());
    }

    @Test
    public void testHaveMore() throws Exception {
        List<String> data = IntStream.range(0, 10).mapToObj(i -> Integer.toString(i)).collect(Collectors.toList());
        TestDataIterator iterator = new TestDataIterator(5, 6, data);

        int size = 0;
        while (iterator.hasNext()) {
            iterator.next();
            size++;
        }

        Assertions.assertEquals(6, size);
        Assertions.assertTrue(iterator.more());
    }

    static class TestDataIterator extends AbstractDataIterator<String> {

        private List<String> data;

        public TestDataIterator(int buffSize, long maxSize, List<String> data) {
            super(buffSize, maxSize);
            this.data = new ArrayList<>(data);
        }

        @Override
        protected void load(List<String> buff, int limit) throws Exception {
            int cursor = 0;
            while (!data.isEmpty() && cursor < limit) {
                String v = data.remove(0);

                buff.add(v);

                cursor++;
            }
        }
    }
}