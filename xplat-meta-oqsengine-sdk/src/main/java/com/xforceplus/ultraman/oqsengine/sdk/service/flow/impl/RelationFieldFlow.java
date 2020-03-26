package com.xforceplus.ultraman.oqsengine.sdk.service.flow.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;
import com.xforceplus.ultraman.oqsengine.sdk.interceptor.MatchRouter;
import com.xforceplus.ultraman.oqsengine.sdk.service.flow.DSLFlow;
import io.vavr.API;
import org.apache.metamodel.data.Row;

/**
 * TODO row to typed
 * how to deal with relation
 * input?
 * output?
 */

public class RelationFieldFlow implements DSLFlow<Row, Relation> {


    @Override
    public Relation apply(Row rel){
//        String relName = rel.getName();
//        IEntityField iEntityField =  new MatchRouter<>()
//                .addRouter("onetoone", x -> {  })
//                .build();
//        rel.setEntityField(iEntityField);
//        return rel;
        return null;
    }
}
