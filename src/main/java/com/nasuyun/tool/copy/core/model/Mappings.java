package com.nasuyun.tool.copy.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class Mappings implements Version.Versionable {

    private JsonNode json;
    private Version version;

    public static Mappings build(JsonNode jsonNode, Version version) {
        return new Mappings(jsonNode, version);
    }

    @Override
    public Version version() {
        return version;
    }
}
