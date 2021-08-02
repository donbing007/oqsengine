package com.xforceplus.ultraman.oqsengine.common.serializable;

import com.xforceplus.ultraman.oqsengine.common.ByteUtil;
import java.io.IOException;

/**
 * 默认的序列化策略实现，使用JDK提供的默认序列化方案。
 *
 * @author dongbin
 * @version 1.00 2020-07-16
 * @since 1.8
 */
public class JdkSerializeStrategy implements SerializeStrategy {

    public JdkSerializeStrategy() {
    }

    @Override
    public byte[] serialize(Object source) throws CanNotBeSerializedException {
        try {
            return ByteUtil.objectToByte(source);
        } catch (IOException ex) {
            throw new CanNotBeSerializedException(ex.getMessage(), ex);
        }
    }

    @Override
    public Object unserialize(byte[] datas) throws CanNotBeUnSerializedException {
        try {
            return ByteUtil.byteToObject(datas);
        } catch (Exception ex) {
            throw new CanNotBeUnSerializedException(ex.getMessage(), ex);
        }
    }
}
