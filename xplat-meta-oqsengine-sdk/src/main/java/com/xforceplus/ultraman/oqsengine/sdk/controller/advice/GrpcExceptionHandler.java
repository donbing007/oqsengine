package com.xforceplus.ultraman.oqsengine.sdk.controller.advice;

import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.concurrent.CompletionException;

/**
 * advice
 * @author admin
 */
@ControllerAdvice
public class GrpcExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> completionException(Exception e) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Response.Error(e.getMessage()));
    }
}
