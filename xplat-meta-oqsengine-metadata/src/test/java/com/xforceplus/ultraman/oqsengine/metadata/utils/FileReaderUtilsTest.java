package com.xforceplus.ultraman.oqsengine.metadata.utils;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
public class FileReaderUtilsTest {

    @Test
    public void getFileNamesInOneDirTest() {
        String path = "src/test/resources/";
        List<String> files = FileReaderUtils.getFileNamesInOneDir(path);

        Assert.assertEquals(1, files.size());
    }
}
