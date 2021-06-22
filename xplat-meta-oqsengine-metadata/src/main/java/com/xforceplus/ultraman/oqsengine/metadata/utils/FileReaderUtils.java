package com.xforceplus.ultraman.oqsengine.metadata.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
public class FileReaderUtils {
    /**
     * 获取某个文件夹下的所有文件,不会递归下层目录,只会读取单层的文件.
     */
    public static List<String> getFileNamesInOneDir(String path) {

        List<String> files = new ArrayList<>();
        File[] tempList = null;
        try {
            File file = new File(path);
            tempList = file.listFiles();

        } catch (Exception e) {
            //  ignore
            return files;
        }
        if (null != tempList) {
            for (int i = 0; i < tempList.length; i++) {
                if (tempList[i].isFile()) {
                    files.add(tempList[i].getName());
                }
            }
        }
        return files;
    }
}
