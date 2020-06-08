//package com.xforceplus.ultraman.oqsengine.sdk.handler;
//
//import com.baomidou.mybatisplus.extension.service.IService;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.xforceplus.ultraman.oqsengine.sdk.command.*;
//import com.xforceplus.ultraman.oqsengine.sdk.staticmode.StaticServiceLoader;
//import com.xforceplus.ultraman.oqsengine.sdk.ui.DefaultUiService;
//import com.xforceplus.xplat.common.DuckType;
//import com.xforceplus.xplat.galaxy.framework.dispatcher.anno.QueryHandler;
//import com.xforceplus.xplat.galaxy.framework.dispatcher.messaging.QueryMessage;
//import io.vavr.Tuple2;
//import io.vavr.control.Either;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.io.InputStream;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//
//@Component
//public class DefaultStaticDictUIServiceHandler implements DefaultUiService {
//
//    @Autowired
//    private StaticServiceLoader staticServerLoader;
//
//    @Autowired
//    private ObjectMapper mapper;
//
//    @QueryHandler
//    @Override
//    public Either<String, Map<String, Object>> singleQuery(SingleQueryCmd cmd) {
//
//        Object ret = staticServerLoader.getService(Long.parseLong(cmd.getBoId()))._2().getById(cmd.getId());
//        if(ret != null){
//            // Convert POJO to Map
//            Map<String, Object> map =
//                    mapper.convertValue(ret, new TypeReference<Map<String, Object>>(){});
//            return Either.right(map);
//        }else{
//            return Either.left("结果为空");
//        }
//    }
//
//    @QueryHandler
//    @Override
//    public Either<String, Long> singleCreate(SingleCreateCmd cmd) {
//
//        Tuple2<Class, IService> tuple = staticServerLoader.getService(Long.parseLong(cmd.getBoId()));
//
//        IService service = tuple._2();
//        Class<?> pojoClass = tuple._1();
//
//        Object input = mapper.convertValue(cmd.getBody(), pojoClass);
//        Boolean ret = service.save(input);
//
//        if(ret) {
//            if (DuckType.coerce(input).quacksLikeA(IdLike.class)) {
//                IdLike idLike = DuckType.coerce(input).to(IdLike.class);
//                return Either.right(idLike.getId());
//            }
//        }
//
//        return Either.left("保存失败");
//    }
//
//    @QueryHandler
//    @Override
//    public Either<String, Integer> singleDelete(SingleDeleteCmd cmd) {
//
//        Tuple2<Class, IService> tuple = staticServerLoader.getService(Long.parseLong(cmd.getBoId()));
//        IService service = tuple._2();
//        boolean ret = service.removeById(cmd.getId());
//        if(ret){
//            return Either.right(1);
//        }
//
//        return Either.left("删除失败");
//    }
//
//    @QueryHandler
//    @Override
//    public Either<String, Integer> singleUpdate(SingleUpdateCmd cmd) {
//
//        Tuple2<Class, IService> tuple = staticServerLoader.getService(Long.parseLong(cmd.getBoId()));
//
//        IService service = tuple._2();
//        Class<?> pojoClass = tuple._1();
//
//        Object input = mapper.convertValue(cmd.getBody(), pojoClass);
//
//        IdLike idLike = DuckType.coerce(input).to(IdLike.class);
//        idLike.setId(cmd.getId());
//
//        boolean ret = service.updateById(input);
//
//        if(ret) {
//            return Either.right(1);
//        }
//
//        return Either.left("保存失败");
//    }
//
//    @QueryHandler
//    @Override
//    public Either<String, Tuple2<Integer, List<Map<String, Object>>>> conditionSearch(ConditionSearchCmd cmd) {
//        return null;
//    }
//
//    @QueryHandler
//    @Override
//    public CompletableFuture<Either<String, String>> conditionExport(QueryMessage<ConditionExportCmd, ?> cmd) {
//        return null;
//    }
//
//    @QueryHandler
//    @Override
//    public Either<String, InputStream> importTemplate(GetImportTemplateCmd cmd) {
//        return null;
//    }
//
//    @QueryHandler
//    @Override
//    public Either<String, String> batchImport(ImportCmd cmd) {
//        return null;
//    }
//}
