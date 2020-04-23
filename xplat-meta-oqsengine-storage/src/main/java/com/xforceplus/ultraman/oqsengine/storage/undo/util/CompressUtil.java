package com.xforceplus.ultraman.oqsengine.storage.undo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/26/2020 2:32 PM
 * 功能描述:
 * 修改历史:
 */
public class CompressUtil {
    final static Logger logger = LoggerFactory.getLogger(CompressUtil.class);

    public static final int BUFFER = 1024;

    /**
     * 把对象转变成二进制
     * @param obj 待转换的对象
     * @return 返回二进制数组
     */
    public static byte[] toByte(Object obj) {
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            //读取对象并转换成二进制数据
            oos.writeObject(obj);
            return bos.toByteArray();
        } catch (IOException e) {
            logger.error("convert object to byte[] failed");
            e.printStackTrace();
        } finally {
            if(oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 把二进制数组的数据转回对象
     * @param b
     * @return
     */
    public static Object toObj(byte[] b) {
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            //读取二进制数据并转换成对象
            bis = new ByteArrayInputStream(b);
            ois = new ObjectInputStream(bis);
            return ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            logger.error("convert byte[] back to object failed");
            e.printStackTrace();
        } finally {
            if(ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 数据压缩
     *
     * @param data
     * @return
     * @throws IOException
     * @throws Exception
     */
    public static byte[] compress(byte[] data) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 压缩
        GZIPOutputStream gos = null;
        try {
            gos = new GZIPOutputStream(baos);
            int count;
            byte buffer[] = new byte[BUFFER];
            while ((count = bais.read(buffer, 0, BUFFER)) != -1) {
                gos.write(buffer, 0, count);
            }
            gos.finish();
            gos.flush();
            baos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            logger.error("compress bytes[] failed");
            e.printStackTrace();
        } finally {
            if(gos != null) {
                try {
                    gos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                baos.close();
                bais.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static byte[] compress(Object obj) {
        return compress(toByte(obj));
    }

    /**
     * 数据解压缩
     *
     * @param data
     * @return
     * @throws IOException
     * @throws Exception
     */
    public static byte[] decompress(byte[] data) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 解压缩
        GZIPInputStream gis = null;
        try {
            gis = new GZIPInputStream(bais);
            int count;
            byte buffer[] = new byte[BUFFER];
            while ((count = gis.read(buffer, 0, BUFFER)) != -1) {
                baos.write(buffer, 0, count);
            }

            return baos.toByteArray();
        } catch (IOException e) {
            logger.error("decompress bytes[] failed");
            e.printStackTrace();
        } finally {
            try {
                gis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                baos.flush();
                baos.close();
                bais.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Object decompressToObj(byte[] data) {
        return toObj(decompress(data));
    }
}
