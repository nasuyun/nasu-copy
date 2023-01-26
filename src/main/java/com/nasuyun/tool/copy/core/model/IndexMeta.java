package com.nasuyun.tool.copy.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class IndexMeta implements Version.Versionable {
    private String name;
    private Settings settings;
    private Mappings mappings;
    private Version version;

    @Override
    public Version version() {
        return version;
    }
}
