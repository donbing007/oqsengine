package com.xforceplus.ultraman.oqsengine.sdk.handler;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.sdk.command.*;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityService;
import com.xforceplus.ultraman.oqsengine.sdk.ui.DefaultUiService;
import com.xforceplus.xplat.galaxy.framework.dispatcher.anno.QueryHandler;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * default ui service handler
 */
public class DefaultEntityServiceHandler implements DefaultUiService {

    @Autowired
    private EntityService entityService;

    private static final String MISSING_ENTITES = "查询记录不存在";

    @QueryHandler(isDefault = true)
    @Override
    public Either<String, Map<String, Object>> singleQuery(SingleQueryCmd cmd) {
        Optional<EntityClass> entityClassOp = entityService.load(cmd.getBoId());

        if (entityClassOp.isPresent()) {
            return entityService.findOne(entityClassOp.get(), Long.parseLong(cmd.getId()));
        } else {
            return Either.left(MISSING_ENTITES);
        }
    }

    @QueryHandler(isDefault = true)
    @Override
    public Either<String, Integer> singleDelete(SingleDeleteCmd cmd) {
        Optional<EntityClass> entityClassOp = entityService.load(cmd.getBoId());

        if (entityClassOp.isPresent()) {
            return entityService.deleteOne(entityClassOp.get(), Long.valueOf(cmd.getId()));
        } else {
            return Either.left(MISSING_ENTITES);
        }
    }

    @QueryHandler(isDefault = true)
    @Override
    public Either<String, Long> singleCreate(SingleCreateCmd cmd) {
        Optional<EntityClass> entityClassOp = entityService.load(cmd.getBoId());

        if (entityClassOp.isPresent()) {
            return entityService.create(entityClassOp.get(), cmd.getBody());
        } else {
            return Either.left(MISSING_ENTITES);
        }
    }

    @QueryHandler(isDefault = true)
    @Override
    public Either<String, Integer> singleUpdate(SingleUpdateCmd cmd) {

        Optional<EntityClass> entityClassOp = entityService.load(cmd.getBoId());

        if (entityClassOp.isPresent()) {
            return entityService.updateById(entityClassOp.get(), cmd.getId(), cmd.getBody());
        } else {
            return Either.left(MISSING_ENTITES);
        }
    }

    @QueryHandler(isDefault = true)
    @Override
    public Either<String, Tuple2<Integer, List<Map<String, Object>>>> conditionSearch(ConditionSearchCmd cmd) {

        Optional<EntityClass> entityClassOp = entityService.load(cmd.getBoId());

        if (entityClassOp.isPresent()) {
            return entityService.findByCondition(entityClassOp.get(), cmd.getConditionQueryRequest());
        } else {
            return Either.left(MISSING_ENTITES);
        }
    }
}
