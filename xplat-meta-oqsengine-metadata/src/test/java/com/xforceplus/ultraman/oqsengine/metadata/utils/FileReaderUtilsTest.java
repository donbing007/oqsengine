package com.xforceplus.ultraman.oqsengine.metadata.utils;

import com.xforceplus.ultraman.oqsengine.metadata.utils.offline.FileReaderUtils;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
public class FileReaderUtilsTest {

    @Test
    public void getFileNamesInOneDirTest() {
        String path = "src/test/resources/local/";
        List<String> files = FileReaderUtils.getFileNamesInOneDir(path);

        Assertions.assertTrue(files.size() > 0);
    }
}
