package com.xforceplus.ultraman.oqsengine.meta.common.utils;

import com.google.protobuf.BoolValue;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.Int64Value;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import com.google.protobuf.util.JsonFormat;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
public class EntityClassStorageHelper {

    private static final JsonFormat.TypeRegistry TYPE_REGISTRY = JsonFormat.TypeRegistry.newBuilder()
        .add(StringValue.getDescriptor()).add(Int64Value.getDescriptor())
        .add(BoolValue.getDescriptor()).add(DoubleValue.getDescriptor()).build();

    /**
     * 使用 initData初始化数据.
     */
    public static String initDataFromFile(String appId, String env, Integer version) {
        String fileName = fileName(appId, version, env);
        InputStream in = EntityClassStorageHelper.class.getResourceAsStream(fileName);

        StringBuilder sb = new StringBuilder();
        try (Scanner scanner = new Scanner(in)) {
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine());
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("read [%s] error, message [%s]", fileName, e.getMessage()));
        }

        return sb.toString();
    }


    private static String fileName(String appId, Integer version, String env) {
        return String.format("/%s_%d_%s.json", appId, version, env);
    }

    /**
     * 将content转为EntityClassSyncRspProto.
     */
    public static EntityClassSyncRspProto toEntityClassSyncRspProto(String content) throws
        InvalidProtocolBufferException {
        EntityClassSyncRspProto.Builder builder = EntityClassSyncRspProto.newBuilder();
        JsonFormat.parser().usingTypeRegistry(TYPE_REGISTRY).ignoringUnknownFields().merge(content, builder);

        return builder.build();
    }
}
