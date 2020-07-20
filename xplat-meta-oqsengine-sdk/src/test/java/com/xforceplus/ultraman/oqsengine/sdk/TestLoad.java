package com.xforceplus.ultraman.oqsengine.sdk;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

@EnableAsync
@ActiveProfiles("new")
public class TestLoad extends ContextWareBaseTest{

    @Autowired
    EntityService entityService;


    @Test
    public void test() {
        Optional<IEntityClass> entityClass = entityService.loadByCode("salesbill");
        Optional<IEntityClass> entityClass2 = entityService.loadByCode("test");

        entityClass.ifPresent(System.out::println);
        entityClass2.ifPresent(System.out::println);
    }


}
