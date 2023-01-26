package com.nasuyun.tool.copy.exception;


/**
 * 服务端错误
 */
public class HttpServerException extends Exception {

    public HttpServerException(Throwable cause) {
        super(cause);
    }

    public HttpServerException(String message, Throwable cause) {
        super(message, cause);
    }

}
