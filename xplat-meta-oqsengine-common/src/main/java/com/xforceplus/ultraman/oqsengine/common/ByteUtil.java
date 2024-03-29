package com.xforceplus.ultraman.oqsengine.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * 一些字节处理的方便方法.
 *
 * @author dongbin
 * @version 1.00 2010-8-23
 * @since 1.5
 */
public class ByteUtil {

    /**
     * 构造新字节时需要与的值表.
     */
    private static final byte[] BUILD_BYTE_TABLE = new byte[] {
        (byte) 128,
        (byte) 64,
        (byte) 32,
        (byte) 16,
        (byte) 8,
        (byte) 4,
        (byte) 2,
        (byte) 1
    };

    private ByteUtil() {
    }

    /**
     * short转换到字节数组.
     *
     * @param number 需要转换的数据。
     * @return 转换后的字节数组。
     */
    public static byte[] shortToByte(short number) {
        byte[] b = new byte[2];
        for (int i = 1; i >= 0; i--) {
            b[i] = (byte) (number % 256);
            number >>= 8;
        }
        return b;
    }

    /**
     * 字节到short转换.
     *
     * @param b short的字节数组
     * @return short数值。
     */
    public static short byteToShort(byte[] b) {
        return byteToShort(b, 0);
    }

    /**
     * 字节到short的转换.从指定的开始位置读取2个字节做为转换数据.
     *
     * @param b     目标字节组.
     * @param start 字节中的开始位置.
     * @return short数值。
     */
    public static short byteToShort(byte[] b, int start) {
        return (short) ((((b[start] & 0xff) << 8) | b[start + 1] & 0xff));
    }

    /**
     * 整型转换到字节数组.
     *
     * @param number 整形数据。
     * @return 整形数据的字节数组。
     */
    public static byte[] intToByte(int number) {
        byte[] b = new byte[4];
        for (int i = 3; i >= 0; i--) {
            b[i] = (byte) (number % 256);
            number >>= 8;
        }
        return b;
    }

    /**
     * 字节数组到整型转换.
     *
     * @param b 整形数据的字节数组。
     * @return 字节数组转换成的整形数据。
     */
    public static int byteToInt(byte[] b) {
        return byteToInt(b, 0);
    }

    /**
     * 字节数组到整形的转换.
     *
     * @param b     目标字节组.
     * @param start 开始位置.
     * @return 整形.
     */
    public static int byteToInt(byte[] b, int start) {
        return ((((b[start] & 0xff) << 24)
            | ((b[start + 1] & 0xff) << 16)
            | ((b[start + 2] & 0xff) << 8)
            | (b[start + 3] & 0xff)));
    }

    /**
     * long转换到字节数组.
     *
     * @param number 长整形数据。
     * @return 长整形转换成的字节数组。
     */
    public static byte[] longToByte(long number) {
        byte[] b = new byte[8];
        for (int i = 7; i >= 0; i--) {
            b[i] = (byte) (number % 256);
            number >>= 8;
        }
        return b;
    }

    /**
     * 字节数组到整型的转换.
     *
     * @param b 长整形字节数组。
     * @return 长整形数据。
     */
    public static long byteToLong(byte[] b) {
        return byteToLong(b, 0);
    }

    /**
     * 字节转换为长整形.
     *
     * @param b     目标字节组.
     * @param start 开始的位置.
     * @return 长整形.
     */
    public static long byteToLong(byte[] b, int start) {
        return ((((long) b[start] & 0xff) << 56)
            | (((long) b[start + 1] & 0xff) << 48)
            | (((long) b[start + 2] & 0xff) << 40)
            | (((long) b[start + 3] & 0xff) << 32)
            | (((long) b[start + 4] & 0xff) << 24)
            | (((long) b[start + 5] & 0xff) << 16)
            | (((long) b[start + 6] & 0xff) << 8)
            | ((long) b[start + 7] & 0xff));
    }

    /**
     * double转换到字节数组.
     *
     * @param d 双精度浮点。
     * @return 双精度浮点的字节数组。
     */
    public static byte[] doubleToByte(double d) {
        byte[] bytes = new byte[8];
        long l = Double.doubleToLongBits(d);
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = Long.valueOf(l).byteValue();
            l = l >> 8;
        }
        return bytes;
    }

    /**
     * 字节数组到double转换.
     *
     * @param b 双精度浮点字节数组。
     * @return 双精度浮点数据。
     */
    public static double byteToDouble(byte[] b) {
        return byteToDouble(b, 0);
    }

    /**
     * 字节数组到double转换.
     *
     * @param b     双精度浮点字节数组。
     * @param start 开始位置.
     * @return 双精度浮点数据。
     */
    public static double byteToDouble(byte[] b, int start) {
        long l;
        l = b[start];
        l &= 0xff;
        l |= ((long) b[start + 1] << 8);
        l &= 0xffff;
        l |= ((long) b[start + 2] << 16);
        l &= 0xffffff;
        l |= ((long) b[start + 3] << 24);
        l &= 0xffffffffL;
        l |= ((long) b[start + 4] << 32);
        l &= 0xffffffffffL;

        l |= ((long) b[start + 5] << 40);
        l &= 0xffffffffffffL;
        l |= ((long) b[start + 6] << 48);
        l &= 0xffffffffffffffL;

        l |= ((long) b[start + 7] << 56);

        return Double.longBitsToDouble(l);
    }

    /**
     * float转换到字节数组.
     *
     * @param d 浮点型数据。
     * @return 浮点型数据转换后的字节数组。
     */
    public static byte[] floatToByte(float d) {
        byte[] bytes = new byte[4];
        int l = Float.floatToIntBits(d);
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = Integer.valueOf(l).byteValue();
            l = l >> 8;
        }
        return bytes;
    }

    /**
     * 字节数组到float的转换.
     *
     * @param b 浮点型数据字节数组。
     * @return 浮点型数据。
     */
    public static float byteToFloat(byte[] b) {
        return byteToFloat(b, 0);
    }

    /**
     * 字节数组到float的转换.
     *
     * @param b 浮点型数据字节数组。
     * @return 浮点型数据。
     */
    public static float byteToFloat(byte[] b, int start) {
        int l;
        l = b[start];
        l &= 0xff;
        l |= ((long) b[start + 1] << 8);
        l &= 0xffff;
        l |= ((long) b[start + 2] << 16);
        l &= 0xffffff;
        l |= ((long) b[start + 3] << 24);
        l &= 0xffffffffL;

        return Float.intBitsToFloat(l);
    }

    /**
     * 字符串到字节数组转换.
     *
     * @param s       字符串。
     * @param charset 字符编码
     * @return 字符串按相应字符编码编码后的字节数组。
     */
    public static byte[] stringToByte(String s, Charset charset) {
        return s.getBytes(charset);
    }

    /**
     * 字节数组带字符串的转换.
     *
     * @param b       字符串按指定编码转换的字节数组。
     * @param charset 字符编码。
     * @return 字符串。
     */
    public static String byteToString(byte[] b, Charset charset) {
        return new String(b, charset);
    }

    /**
     * 对象转换成字节数组.
     *
     * @param obj 字节数组。
     * @return 对象实例相应的序列化后的字节数组。
     */
    public static byte[] objectToByte(Object obj) throws IOException {
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buff);
        out.writeObject(obj);
        try {
            return buff.toByteArray();
        } finally {
            out.close();
        }
    }

    /**
     * 序死化字节数组转换成实际对象.
     *
     * @param b 字节数组。
     * @return 对象。
     */
    public static Object byteToObject(byte[] b)
        throws IOException, ClassNotFoundException {
        ByteArrayInputStream buff = new ByteArrayInputStream(b);
        ObjectInputStream in = new ObjectInputStream(buff);
        Object obj = in.readObject();
        try {
            return obj;
        } finally {
            in.close();
        }
    }

    /**
     * 比较两个字节的每一个bit位是否相等.
     *
     * @param a 比较的字节.
     * @param b 比较的字节
     * @return ture 两个字节每一位都相等,false有至少一位不相等.
     */
    public static boolean equalsBit(byte a, byte b) {
        return Arrays.equals(byteToBitArray(a), byteToBitArray(b));
    }

    /**
     * 比较两个数组中的每一个字节,两个字节必须二进制字节码每一位都相同才表示两个
     * byte相同.
     *
     * @param a 比较的字节数组.
     * @param b 被比较的字节数.
     * @return ture每一个元素的每一位两个数组都是相等的, false至少有一位不相等.
     */
    public static boolean equalsBit(byte[] a, byte[] b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }

        int length = a.length;
        if (b.length != length) {
            return false;
        }

        for (int count = 0; count < a.length; count++) {
            if (!equalsBit(a[count], b[count])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将一个byte数组转换成bit位表示的的字符串表示形式.
     *
     * @param bytes 目标字节数组.
     * @return bit的字符串表示形式.
     */
    public static String bitsString(byte[] bytes) {
        StringBuilder buff = new StringBuilder();
        for (byte b : bytes) {
            doBitString(b, buff);
        }
        return buff.toString();
    }

    /**
     * 返回某个字节的bit组成的字符串.
     *
     * @param b 字节.
     * @return Bit位组成的字符串.
     */
    public static String bitString(byte b) {
        StringBuilder buff = new StringBuilder();
        doBitString(b, buff);
        return buff.toString();
    }

    /**
     * 计算出给定byte中的每一位,并以一个布尔数组返回.
     * true表示为1,false表示为0.
     *
     * @param b 字节.
     * @return 指定字节的每一位bit组成的数组.
     */
    public static boolean[] byteToBitArray(byte b) {
        boolean[] buff = new boolean[8];
        int index = 0;
        for (int i = 7; i >= 0; i--) {
            buff[index++] = ((b >>> i) & 1) == 1;
        }
        return buff;
    }

    /**
     * 返回指定字节中指定bit位,true为1,false为0.
     * 指定的位从0-7,超出将抛出数据越界异常.
     *
     * @param b     需要判断的字节.
     * @param index 字节中指定位.
     * @return 指定位的值.
     */
    public static boolean byteBitValue(byte b, int index) {
        return byteToBitArray(b)[index];
    }

    /**
     * 根据布尔数组表示的二进制构造一个新的字节.
     *
     * @param values 布尔数组,其中true表示为1,false表示为0.
     * @return 构造的新字节.
     */
    public static byte buildNewByte(boolean[] values) {
        byte b = 0;
        for (int i = 0; i < 8; i++) {
            if (values[i]) {
                b |= BUILD_BYTE_TABLE[i];
            }
        }
        return b;
    }

    /**
     * 将指定字节中的某个bit位替换成指定的值,true代表1,false代表0.
     *
     * @param b        需要被替换的字节.
     * @param index    位的序号,从0开始.超过7将抛出越界异常.
     * @param newValue 新的值.
     * @return 替换好某个位值的新字节.
     */
    public static byte changeByteBitValue(byte b, int index, boolean newValue) {
        boolean[] bitValues = byteToBitArray(b);
        bitValues[index] = newValue;
        return buildNewByte(bitValues);
    }

    private static void doBitString(byte b, StringBuilder buff) {
        boolean[] array = byteToBitArray(b);
        for (int i = 0; i < array.length; i++) {
            buff.append(array[i] ? 1 : 0);
        }
    }
}
