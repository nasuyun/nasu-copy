package com.nasuyun.tool.copy.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Iterator;

public class Json {

    public static JsonNode toJson(String content) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readTree(content);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toString(Object obj) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode firstNode(JsonNode jsonNode) {
        Iterator<JsonNode> next = jsonNode.iterator();
        return next.hasNext() ? next.next() : null;
    }
}
