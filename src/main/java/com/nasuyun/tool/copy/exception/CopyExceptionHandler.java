package com.nasuyun.tool.copy.exception;

import org.apache.http.client.HttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 统一转Http层协议异常
 */
@ControllerAdvice
public class CopyExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(CopyExceptionHandler.class);

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResponseEntity exceptionHandler(Exception e) {
        RestResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, e);
        // 转Http协议异常
        if (e instanceof HttpResponseException) {
            HttpResponseException ex = (HttpResponseException) e;
            ResponseEntity responseEntity = new ResponseEntity(e.getMessage(), HttpStatus.valueOf(ex.getStatusCode()));
            return responseEntity;
        }
        ResponseEntity responseEntity = new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        return responseEntity;
    }

}