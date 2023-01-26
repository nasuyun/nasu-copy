package com.nasuyun.tool.copy.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class Template {
    private String name;
    private JsonNode json;
}
