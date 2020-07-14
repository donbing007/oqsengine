package com.xforceplus.ultraman.oqsengine.sdk.controller;

import com.xforceplus.ultraman.oqsengine.sdk.service.ExportSink;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 *
 */
@RequestMapping("/download/")
public class DownloadController {

    private final ExportSink exportSink;

    public DownloadController(ExportSink exportSink) {
        this.exportSink = exportSink;
    }

    //TODO default is csv
    @GetMapping(value = "/file/{token}")
    public ResponseEntity<StreamingResponseBody> uploadFile(@PathVariable String token
            , @RequestParam(value = "filename", required = false) String filename) {

        InputStream input = null;
        MediaType mediaType = null;
        String innerFileName = null;
        if (filename != null) {
            innerFileName = filename;
        } else {
            innerFileName = token;
        }

        try {
            input = exportSink.getInputStream(token);
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        } catch (Exception e) {
            e.printStackTrace();
        }

        InputStream finalInput = input;
        StreamingResponseBody responseBody = outputStream -> {
            StreamUtils.copy(finalInput, outputStream);
            outputStream.close();
        };

        String encodedName = innerFileName;
        try {
            encodedName = URLEncoder.encode(innerFileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + encodedName + ".csv")
                .contentType(mediaType)
                .body(responseBody);
    }
}
