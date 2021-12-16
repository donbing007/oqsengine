package com.xforceplus.ultraman.oqsengine.metadata.utils.offline;

import com.google.protobuf.BoolValue;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.Int64Value;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import com.google.protobuf.util.JsonFormat;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
public class OffLineMetaHelper {

    static final Logger LOGGER = LoggerFactory.getLogger(OffLineMetaHelper.class);

    private static final int SPLITTER_LENGTH = 3;

    private static final JsonFormat.TypeRegistry TYPE_REGISTRY = JsonFormat.TypeRegistry.newBuilder()
        .add(StringValue.getDescriptor()).add(Int64Value.getDescriptor())
        .add(BoolValue.getDescriptor()).add(DoubleValue.getDescriptor()).build();

    /**
     * 使用 initData初始化数据.
     */
    public static String initDataFromInputStream(String appId, String env, Integer version, InputStream in) {
        StringBuilder sb = new StringBuilder();
        try (Scanner scanner = new Scanner(in, "UTF-8")) {
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine());
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("read [$path/%s_%d_%s.json] error, message [%s]", appId, version, env, e.getMessage()));
        }

        return sb.toString();
    }

    /**
     * 使用 initData初始化数据.
     */
    public static String initDataFromFilePath(String appId, String env, Integer version, String path)
        throws IOException {
        File file = new File(path);
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(file);
            return initDataFromInputStream(appId, env, version, fis);
        } finally {
            if (null != fis) {
                fis.close();
            }
        }
    }

    /**
     * 将content转为EntityClassSyncRspProto.
     */
    public static EntityClassSyncRspProto toEntityClassSyncRspProto(String content) throws InvalidProtocolBufferException {
        EntityClassSyncRspProto.Builder builder = EntityClassSyncRspProto.newBuilder();
        JsonFormat.parser().usingTypeRegistry(TYPE_REGISTRY).ignoringUnknownFields().merge(content, builder);

        return builder.build();
    }

    /**
     * splitMetaFromFileName.
     */
    public static String[] splitMetaFromFileName(String name) {
        if (null != name && !name.isEmpty()) {
            String[] splitter = name.split("_");
            if (splitter.length == SPLITTER_LENGTH && Integer.parseInt(splitter[1]) > 0) {
                return splitter;
            } else {
                String error = String.format("splitter meta-file failed, name invalid, %s", name);
                LOGGER.warn(error);
                throw new RuntimeException(error);
            }
        }

        throw new RuntimeException("file name invalid or null.");
    }

    /**
     * 判断是否为合法的loadPath
     * @param path
     * @return
     */
    public static boolean isValidPath(String path) {
        return null != path && !path.isEmpty() && !path.equals("-");
    }
}
