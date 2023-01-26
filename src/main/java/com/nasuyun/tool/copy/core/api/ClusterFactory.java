package com.nasuyun.tool.copy.core.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.nasuyun.tool.copy.utils.Http;
import com.nasuyun.tool.copy.core.model.Version;
import lombok.extern.slf4j.Slf4j;

import static com.nasuyun.tool.copy.utils.Json.toJson;

@Slf4j
public class ClusterFactory {

    public static Cluster factory(ConnectInfo connectInfo) {
        Http http = new Http(connectInfo.getEndpoint(), connectInfo.getUsername(), connectInfo.getPassword());
        try {
            JsonNode jsonNode = toJson(http.get("/"));
            String versionString = jsonNode.get("version").get("number").asText();
            Version version = Version.of(versionString);
            if (version.majorEqual(6)) {
                return new ClusterV6(connectInfo, version);
            } else if (version.majorEqual(7)) {
                return new ClusterV7(connectInfo, version);
            } else {
                throw new UnsupportedOperationException("cluster version upsupported:" + versionString);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
