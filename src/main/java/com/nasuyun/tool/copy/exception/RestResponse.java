package com.nasuyun.tool.copy.exception;

import com.nasuyun.tool.copy.utils.Json;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.http.HttpStatus;

import java.util.Date;
import java.util.Map;

/**
 * 对应React ResponseError
 */
public class RestResponse<T> {

    public final HttpStatus status;
    public final String error;
    public final T data;

    public RestResponse(HttpStatus status, String errorMessage, T data) {
        this.status = status;
        this.error = errorMessage;
        this.data = data;
    }

    public static <T> RestResponse success(T data) {
        return new RestResponse(HttpStatus.OK, null, data);
    }

    public static RestResponse error(HttpStatus status, String errorMessage) {
        return new RestResponse(status, errorMessage, errorMessage);
    }

    public static RestResponse error(HttpStatus status, Exception throwable) {
        return new RestResponse(status, throwable.getMessage(), throwable.getMessage());
    }

    public static RestResponse error(HttpStatus status, Throwable throwable) {
        return new RestResponse(status, throwable.getMessage(), throwable.getMessage());
    }

    @Override
    public String toString() {
        return error != null ? Json.toString(Map.of(
                "timestamp", DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"),
                "code", status.value(),
                "status", status,
                "error", error
        )) : Json.toString(this);
    }
}