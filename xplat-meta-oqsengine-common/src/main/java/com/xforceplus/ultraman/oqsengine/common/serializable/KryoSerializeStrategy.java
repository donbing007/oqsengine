package com.xforceplus.ultraman.oqsengine.common.serializable;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;

/**
 * 基于kryo 的序列化策略.
 *
 * @author dongbin
 * @version 0.1 2021/07/16 13:28
 * @since 1.8
 */
public class KryoSerializeStrategy implements SerializeStrategy {

    private Kryo kryo = new Kryo();

    public KryoSerializeStrategy() {
        kryo.setRegistrationRequired(false);
    }

    @Override
    public byte[] serialize(Serializable source) throws CanNotBeSerializedException {
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        try (Output output = new Output(buff)) {
            try {
                kryo.writeObject(output, source);
            } catch (Exception ex) {
                throw new CanNotBeSerializedException(ex.getMessage(), ex);
            }
        }

        return buff.toByteArray();
    }

    @Override
    public <T> T unserialize(byte[] datas, Class<T> clazz) throws CanNotBeUnSerializedException {
        try (Input input = new Input(new ByteArrayInputStream(datas))) {
            try {
                return kryo.readObject(input, clazz);
            } catch (Exception ex) {
                throw new CanNotBeUnSerializedException(ex.getMessage(), ex);
            }
        }
    }
}
