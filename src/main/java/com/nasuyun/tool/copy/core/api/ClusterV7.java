package com.nasuyun.tool.copy.core.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.nasuyun.tool.copy.core.model.IndexMeta;
import com.nasuyun.tool.copy.core.model.Version;
import com.nasuyun.tool.copy.utils.Json;
import com.nasuyun.tool.copy.utils.TemplateFileUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.nasuyun.tool.copy.utils.Json.firstNode;

@Slf4j
public class ClusterV7 extends ClusterBase {

    public ClusterV7(ConnectInfo connectInfo, Version version) {
        super(connectInfo, version);
    }

    @Override
    public Response create(IndexMeta sourceIndexMeta) {
        if (sourceIndexMeta.version().majorEqual(7) || sourceIndexMeta.getMappings().getJson().isEmpty()) {
            return super.create(sourceIndexMeta);
        }
        // 来源为6版本的去掉_doc
        if (sourceIndexMeta.version().majorLte(6)) {
            JsonNode mappingNode = sourceIndexMeta.getMappings().getJson();
            String mappingString = Json.toString(firstNode(mappingNode));
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
        throw new UnsupportedOperationException("create index not support for dest elasticsearch version:" + sourceIndexMeta.version());

    }
}
