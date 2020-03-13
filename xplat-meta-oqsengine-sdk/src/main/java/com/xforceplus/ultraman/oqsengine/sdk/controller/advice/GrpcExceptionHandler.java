package com.xforceplus.ultraman.oqsengine.sdk.controller.advice;

import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.Response;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.concurrent.CompletionException;

//TODO check if canbe
@RestControllerAdvice
public class GrpcExceptionHandler {
    @ExceptionHandler(CompletionException.class)
    public Response completionException(Exception e) {
        return Response.Error(e.getMessage());
    }
}