package com.nasuyun.tool.copy.core.api;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class HealthResponse {
    boolean connected;
    String clusterName;
    String status;
    JsonNode json;
}
