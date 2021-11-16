package com.xforceplus.ultraman.oqsengine.idgenerator.exception;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 5/9/21 10:19 PM
 */
public class IDGeneratorException extends RuntimeException {

    public IDGeneratorException() {
        super();
    }

    public IDGeneratorException(String message) {
        super(message);
    }

    public IDGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public IDGeneratorException(Throwable cause) {
        super(cause);
    }
}
