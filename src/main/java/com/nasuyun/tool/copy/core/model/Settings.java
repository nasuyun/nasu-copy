package com.nasuyun.tool.copy.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class Settings {

    private int shards;
    private int replicas;

    public static Settings build(JsonNode settingsNode) {
        JsonNode body = settingsNode.get("index");
        int shards = body.get("number_of_shards").asInt();
        int replicas = body.get("number_of_replicas").asInt();
        return new Settings(shards, replicas);
    }
}
