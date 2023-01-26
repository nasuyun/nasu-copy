package com.nasuyun.tool.copy.core.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public class Response {
    boolean success;
    String message;

    public static Response OK = new Response(true, null);

    public static Response failure(String message) {
        return new Response(false, message);
    }
}
