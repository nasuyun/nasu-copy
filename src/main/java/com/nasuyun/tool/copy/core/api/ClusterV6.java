package com.nasuyun.tool.copy.core.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.nasuyun.tool.copy.utils.Json;
import com.nasuyun.tool.copy.utils.TemplateFileUtils;
import com.nasuyun.tool.copy.core.model.IndexMeta;
import com.nasuyun.tool.copy.core.model.Version;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class ClusterV6 extends ClusterBase {

    public ClusterV6(ConnectInfo connectInfo, Version version) {
        super(connectInfo, version);
    }

    @Override
    public Response create(IndexMeta sourceIndexMeta) {
        if (sourceIndexMeta.version().majorLte(6) || sourceIndexMeta.getMappings().getJson().isEmpty()) {
            return super.create(sourceIndexMeta);
        }
        // 来源为7版本的添加_doc
        JsonNode mappingNode = sourceIndexMeta.getMappings().getJson();
        String mappingString = Json.toString(Map.of("_doc", mappingNode));
        String body = TemplateFileUtils.replace("templates/create-index.txt",
                Map.of("number_of_shards", sourceIndexMeta.getSettings().getShards(),
                        "number_of_replicas", sourceIndexMeta.getSettings().getReplicas(),
                        "mappings", mappingString)
        );
        try {
            http().put("/" + sourceIndexMeta.getName(), body);
        } catch (Exception e) {
            return Response.failure(e.getMessage());
        }
        return Response.OK;
    }
}
