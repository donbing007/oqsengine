package com.xforceplus.ultraman.oqsengine.common.serializable;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 基于Hessian2.0协议的序列化策略实现类.
 * 此实现是线程安全的.
 *
 * @author dongbin
 * @author mike
 * @version 1.00 2020-07-16
 * @since 1.8
 */
public class HessianSerializeStrategy implements SerializeStrategy {

    private static final SerializerFactory SERIALIZER_FACTORY = new SerializerFactory();

    @Override
    public byte[] serialize(Object source) throws CanNotBeSerializedException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Hessian2Output out = new Hessian2Output(os);
        //这里调用方法的原因是hessian内部没有为此设定一个默认値,每个实例都会创建SerializerFactory.
        //创建SerializerFactory用时很长.
        out.setSerializerFactory(SERIALIZER_FACTORY);
        try {
            out.writeObject(source);
            out.close();
        } catch (IOException ex) {
            throw new CanNotBeSerializedException(ex.getMessage(), ex);
        }

        return os.toByteArray();
    }

    @Override
    public Object unserialize(byte[] datas) throws CanNotBeUnSerializedException {
        ByteArrayInputStream os = new ByteArrayInputStream(datas);
        Hessian2Input input = new Hessian2Input(os);
        input.setSerializerFactory(SERIALIZER_FACTORY);
        Object obj = null;
        try {
            obj = input.readObject();
        } catch (IOException ex) {
            throw new CanNotBeUnSerializedException(ex.getMessage(), ex);
        }

        return obj;
    }
}
