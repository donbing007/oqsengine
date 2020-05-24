package com.xforceplus.ultraman.oqsengine.sdk.command;

import org.springframework.web.multipart.MultipartFile;

/**
 * import Cmd
 */
public class ImportCmd implements MetaDataLikeCmd {

    private String boId;

    private String version;

    private MultipartFile file;

    public ImportCmd(String boId, String version, MultipartFile file) {
        this.boId = boId;
        this.version = version;
        this.file = file;
    }

    @Override
    public String getBoId() {
        return boId;
    }

    @Override
    public String version() {
        return version;
    }

    @Override
    public void clearVersion() {
        this.version = null;
    }

    public MultipartFile getFile() {
        return file;
    }
}
