package com.xforceplus.ultraman.oqsengine.core.service.impl.calculator.mock;

import com.xforceplus.ultraman.oqsengine.idgenerator.generator.IDGenerator;
import com.xforceplus.ultraman.oqsengine.idgenerator.generator.IDGeneratorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author j.xu
 * @version 0.1 2021/05/2021/5/18
 * @since 1.8
 */
public class MockIDGeneratorFactory implements IDGeneratorFactory {

    public static final Map<String, IDGenerator> repository = new ConcurrentHashMap<>();

    @Override
    public IDGenerator getIdGenerator(String bizType) {

        repository.computeIfAbsent(bizType, MockIDGenerator::new);

        return repository.get(bizType);
    }

    public static class MockIDGenerator implements IDGenerator {

        private AtomicLong id;
        private String bizType;

        public MockIDGenerator(String bizType) {
            this.id = new AtomicLong(0);
            this.bizType = bizType;
        }

        @Override
        public String nextId() {
            return bizType + "-" + id.getAndIncrement();
        }

        @Override
        public List<String> nextIds(Integer batchSize) {
            List<String> batchIds = new ArrayList<>();
            for (int i = 0; i < batchSize; i++) {
                batchIds.add(nextId());
            }
            return batchIds;
        }
    }
}
