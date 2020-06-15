package com.xforceplus.ultraman.oqsengine.sdk.store;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.diff.JsonDiff;
import com.google.protobuf.util.JsonFormat;
import com.xforceplus.ultraman.metadata.grpc.BoUp;
import com.xforceplus.ultraman.metadata.grpc.ModuleUpResult;
import com.xforceplus.ultraman.metadata.grpc.Relation;
import org.junit.Test;

import java.io.IOException;

public class NewJsonConfiguration {

    @Test
    public void testJsonDiff() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode beforeNode = mapper.readTree("{\"name\":\"jojo\"}");
        JsonNode afterNode = mapper.readTree("{\"name\":\"jojo2\"}");
        JsonNode patch = JsonDiff.asJson(beforeNode, afterNode);
        String diffs = patch.toString();

        System.out.println(diffs);
    }


    private ModuleUpResult manyToOneNew() {
        return ModuleUpResult
                .newBuilder()
                .setVersion("0.0.1")
                .setId(111111111111L)
                .addBoUps(BoUp
                        .newBuilder()
                        .setId("1")
                        .setCode("main")
                        .addRelations(Relation.newBuilder()
                                .setId("10001")
                                .setRelationType("ManyToOne")
                                .setRelName("rel1")
                                .setJoinBoId("2")
                                .setBoId("1")
                                .build())
                        .addRelations(Relation.newBuilder()
                                .setId("10002")
                                .setRelationType("ManyToOne")
                                .setRelName("rel2")
                                .setJoinBoId("2")
                                .setBoId("1")
                                .build())
                        .addFields(com.xforceplus.ultraman.metadata.grpc.Field
                                .newBuilder()
                                .setCode("id")
                                .setSearchable("1")
                                .setId("1000001")
                                .setFieldType("Long")
                                .setIdentifier("1")
                                .build())
                        .addFields(com.xforceplus.ultraman.metadata.grpc.Field
                                .newBuilder()
                                .setCode("field1")
                                .setSearchable("1")
                                .setId("1003")
                                .build())
                        .addFields(com.xforceplus.ultraman.metadata.grpc.Field
                                .newBuilder()
                                .setCode("field2")
                                .setSearchable("0")
                                .setId("1004")
                                .build())
                        .addFields(com.xforceplus.ultraman.metadata.grpc.Field
                                .newBuilder()
                                .setCode("field3")
                                .setSearchable("0")
                                .setId("1005")
                                .build())
                        .build())
                .addBoUps(BoUp
                        .newBuilder()
                        .setId("2")
                        .setCode("rel1")
                        .addFields(com.xforceplus.ultraman.metadata.grpc.Field
                                .newBuilder()
                                .setCode("field21")
                                .setSearchable("1")
                                .setFieldType("String")
                                .setId("2001")
                                .build())
                        .build())
                .build();
    }

    @Test
    public void testModuleUp() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        ModuleUpResult result = manyToOneNew();


        String json = JsonFormat.printer().print(result);



        ModuleUpResult.Builder result1 = ModuleUpResult.newBuilder();
        JsonFormat.parser().merge(json, result1);
        System.out.println(result1.build());

        JsonNode beforeNode = mapper.readTree(json);

        JsonNode afterNode = mapper.readTree("{\"name\":\"jojo2\"}");


        JsonNode patch = JsonDiff.asJson(beforeNode, afterNode);

        System.out.println(patch.toString());



        result.getVersion();
        result.getVersion();
    }
}
