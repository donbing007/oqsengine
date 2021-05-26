package com.xforceplus.ultraman.oqsengine.idgenerator.client;

import com.xforceplus.ultraman.oqsengine.idgenerator.generator.IDGenerator;
import com.xforceplus.ultraman.oqsengine.idgenerator.generator.IDGeneratorFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 5/9/21 11:35 PM
 */
public class BizIDGenerator {


    @Resource
    IDGeneratorFactory idGeneratorFactory;

    public  String nextId(String bizType) {
        if(bizType == null) {
            throw new IllegalArgumentException("type is null");
        }
        IDGenerator idGenerator = idGeneratorFactory.getIdGenerator(bizType);
        return idGenerator.nextId();
    }

    public  List<String> nextId(String bizType, Integer batchSize) {
        if(batchSize == null) {
            String id = nextId(bizType);
            List<String> list = new ArrayList<>();
            list.add(id);
            return list;
        }
        IDGenerator idGenerator = idGeneratorFactory.getIdGenerator(bizType);
        return idGenerator.nextIds(batchSize);
    }

}