package com.xforceplus.ultraman.oqsengine.common.gzip;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * desc :
 * name : ZipUtils
 *
 * @author : xujia
 * date : 2021/4/14
 * @since : 1.8
 */
public class ZipUtils {
    public static String zip(String content) throws IOException {
        content = content.replaceAll("\\n", "\\\\n");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(content.getBytes(StandardCharsets.UTF_8));
        gzip.close();

        byte[] bytes = out.toByteArray();
        byte[] encode = Base64.getEncoder().encode(bytes);

        return new String(encode, StandardCharsets.UTF_8);
    }

    public static String unzip(String content) throws IOException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        byte[] decodedBytes = Base64.getDecoder().decode(bytes);

        GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(decodedBytes));
        BufferedReader bf = new BufferedReader(new InputStreamReader(gzip, "UTF-8"));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line=bf.readLine())!=null) {
            sb.append(line);
        }

        String result = sb.toString();

        return result.replaceAll("\\\\n", "\n");
    }
}
