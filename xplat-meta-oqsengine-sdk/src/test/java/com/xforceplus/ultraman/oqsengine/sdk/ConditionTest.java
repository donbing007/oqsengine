package com.xforceplus.ultraman.oqsengine.sdk;

import akka.actor.ActorSystem;
import akka.grpc.GrpcClientSettings;
import akka.stream.ActorMaterializer;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.sdk.service.*;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityService;
import com.xforceplus.ultraman.oqsengine.sdk.service.impl.DefaultHandleQueryValueService;
import com.xforceplus.ultraman.oqsengine.sdk.service.impl.DefaultHandleResultValueService;
import com.xforceplus.ultraman.oqsengine.sdk.service.impl.DefaultHandleValueService;
import com.xforceplus.ultraman.oqsengine.sdk.service.impl.EntityServiceImpl;
import com.xforceplus.ultraman.oqsengine.sdk.util.EntityClassToGrpcConverter;
import com.xforceplus.ultraman.oqsengine.sdk.util.RequestBuilder;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionOp;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;
import com.xforceplus.xplat.galaxy.framework.context.ContextService;
import com.xforceplus.xplat.galaxy.framework.context.DefaultContextServiceImpl;
import com.xforceplus.xplat.galaxy.framework.context.ThreadLocalContextHolder;
import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.*;

/**
 * Condition test
 */
public class ConditionTest {

    @Test
    public void testQueryNull() {
        String x = null;
        ConditionQueryRequest abc = new RequestBuilder()
                .field("abc", ConditionOp.eq, x)
                .build();

        //long id, String code, Collection<IEntityField> fields
        //long id, String name, FieldType fieldType
        EntityField field = new EntityField(1L, "abc", FieldType.STRING);
        EntityClass entityClass = new EntityClass(1L, "test", Collections.singleton(field));
        EntityClassToGrpcConverter.toSelectByCondition(entityClass, null, abc);
    }


    private EntityClass entityClass(){
        EntityField field = new EntityField(1L, "abc", FieldType.STRING);
        EntityClass entityClass = new EntityClass(1L, "test", Collections.singleton(field));
        return entityClass;
    }

    @Test
    public void testCreateNull() {
        String x = null;

        Map<String, Object> create = new HashMap<>();

        create.put("abc", null);

        EntityField field = new EntityField(1L, "abc", FieldType.STRING);
        EntityClass entityClass = new EntityClass(1L, "test", Collections.singleton(field));
        DefaultHandleValueService handleValueService = new DefaultHandleValueService(Collections.emptyList(), Collections.emptyList());

        List<ValueUp> valueUps = handleValueService.handlerValue(entityClass, create, OperationType.CREATE);
        EntityClassToGrpcConverter.toEntityUp(entityClass, null, valueUps);
    }


    @Test
    public void testSdkWithoutSpring(){

        ActorSystem system = ActorSystem.create();
        ActorMaterializer mat = ActorMaterializer.create(system);

        GrpcClientSettings settings = GrpcClientSettings.fromConfig("EntityService", system);


        EntityServiceClient entityServiceClient = EntityServiceClient.create(settings, mat, system.dispatcher());

        HandleValueService handleValueService = new DefaultHandleValueService(Collections.emptyList(), Collections.emptyList());
        HandleQueryValueService queryValueService = new DefaultHandleQueryValueService(Collections.emptyList());
        HandleResultValueService resultValueService = new DefaultHandleResultValueService(Collections.emptyList(), Collections.emptyList());

        ThreadLocalContextHolder holder = new ThreadLocalContextHolder();
        ContextService contextService = new DefaultContextServiceImpl(holder);


        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        doNothing().when(publisher).publishEvent(any(Object.class));

        EntityService service = new EntityServiceImpl(null, entityServiceClient, contextService);
        setService((EntityServiceImpl)service, handleValueService, "handlerValueService");
        setService((EntityServiceImpl)service, queryValueService, "handleQueryValueService");
        setService((EntityServiceImpl)service, resultValueService, "handleResultValueService");
        setService((EntityServiceImpl)service, publisher, "publisher");

        Map<String, Object> body = new HashMap<>();
        System.out.println(service.create(entityClass(), body));

    }

    private void setService(EntityServiceImpl service, Object obj, String name){
        try {
            Field handlerValueService = service.getClass().getDeclaredField(name);
            handlerValueService.setAccessible(true);
            handlerValueService.set(service, obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
