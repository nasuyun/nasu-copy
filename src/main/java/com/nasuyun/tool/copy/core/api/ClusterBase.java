package com.nasuyun.tool.copy.core.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.nasuyun.tool.copy.core.model.*;
import com.nasuyun.tool.copy.utils.Http;
import com.nasuyun.tool.copy.utils.Json;
import com.nasuyun.tool.copy.utils.TemplateFileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import static com.nasuyun.tool.copy.utils.Json.firstNode;
import static com.nasuyun.tool.copy.utils.Json.toJson;
import static com.nasuyun.tool.copy.utils.Simplify.ignoreExc;

@Slf4j
public class ClusterBase implements Cluster {

    private Http http;
    private Version version;

    public ClusterBase(ConnectInfo connectInfo, Version version) {
        this.http = new Http(connectInfo.getEndpoint(), connectInfo.getUsername(), connectInfo.getPassword());
        this.version = version;
    }

    protected Http http() {
        return http;
    }

    @Override
    public Version version() {
        return version;
    }

    @Override
    public HealthResponse health() {
        try {
            String content = http().get("/_cluster/health");
            JsonNode json = toJson(content);
            HealthResponse response = new HealthResponse();
            response.setStatus(json.get("status").asText());
            response.setClusterName(json.get("cluster_name").asText());
            response.setJson(json);
            return response;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public IndexInfo[] indices() {
        List<IndexInfo> indices = new ArrayList<>();
        String s = http().get("/_cat/indices?h=index,status,health,docsCount,store.size");
        try (BufferedReader reader = new BufferedReader(new StringReader(s))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (Strings.isNotEmpty(line)) {
                    String[] val = line.split("\\s+");
                    ignoreExc(() -> {
                        IndexInfo info = new IndexInfo();
                        info.index = val[0];
                        info.status = val[1];
                        if ("closed".equalsIgnoreCase(info.status) == false) {
                            info.health = val[2];
                            info.docs = val[3];
                            info.size = val[4];
                        }
                        indices.add(info);
                    });
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        indices.sort((o1, o2) -> o1.index.compareTo(o2.getIndex()));
        return indices.toArray(new IndexInfo[indices.size()]);
    }

    @Override
    public IndexMeta get(String index) {
        String s = http().get("/" + index);
        JsonNode indexNode = toJson(s);
        JsonNode body = firstNode(indexNode);
        JsonNode settingsNode = body.get("settings");
        JsonNode mappingsNode = body.get("mappings");
        IndexMeta indexMeta = new IndexMeta(index, Settings.build(settingsNode), Mappings.build(mappingsNode, version), version);
        return indexMeta;
    }

    @Override
    public Response create(IndexMeta indexMeta) {
        String body = TemplateFileUtils.replace("templates/create-index.txt",
                Map.of("number_of_shards", indexMeta.getSettings().getShards(),
                        "number_of_replicas", indexMeta.getSettings().getReplicas(),
                        "mappings", indexMeta.getMappings().getJson())
        );
        try {
            http().put("/" + indexMeta.getName(), body);
        } catch (Exception e) {
            return Response.failure(e.getMessage());
        }
        return Response.OK;
    }

    @Override
    public Response delete(String index) {
        try {
            http().delete("/" + index);
            return Response.OK;
        } catch (Exception e) {
            return Response.failure(e.getMessage());
        }
    }

    @Override
    public ScrollResponse scroll(String index, String time, int batchSize) {
        String response = http().get("/" + index + "/_search", Map.of("scroll", "5m", "size", "" + batchSize));
        JsonNode jsonNode = Json.toJson(response);
        String scrollId = jsonNode.get("_scroll_id").asText();
        ArrayNode hits = (ArrayNode) jsonNode.get("hits").get("hits");
        ScrollResponse scrollResponse = new ScrollResponse(scrollId, hits);
        return scrollResponse;
    }

    @Override
    public ScrollResponse scrollNext(String index, String scrollId, int batchSize) {
        String response = http().post("/_search/scroll", null, Json.toString(Map.of("scroll", "5m", "scroll_id", scrollId)));
        JsonNode jsonNode = Json.toJson(response);
        String nextScrollId = jsonNode.get("_scroll_id").asText();
        ArrayNode hits = (ArrayNode) jsonNode.get("hits").get("hits");
        ScrollResponse scrollResponse = new ScrollResponse(nextScrollId, hits);
        return scrollResponse;
    }

    @Override
    public Response bulk(ScrollResponse response) {
        try {
            // TODO retry
            String body = buildBulkRequest(response.getHits());
            http().post("/_bulk", body);
            return Response.OK;
        } catch (Exception e) {
            return Response.failure(e.getMessage());
        }
    }

    @Override
    public long count(String index) {
        String c = http().get("/" + index + "/_count");
        return toJson(c).get("count").asLong();
    }

    @Override
    public Response replicas(String index, int replicas) {
        try {
            Map body = Map.of("index", Map.of("number_of_replicas", 1));
            http().put("/" + index + "/_settings", Json.toString(body));
            return Response.OK;
        } catch (Exception e) {
            return Response.failure(e.getMessage());
        }
    }

    @Override
    public Pipeline[] pipelines() {
        try {
            String content = http().get("/_ingest/pipeline");
            JsonNode jsonNode = toJson(content);
            List<Pipeline> pipelines = new ArrayList<>();
            jsonNode.fieldNames().forEachRemaining(key -> pipelines.add(new Pipeline(key, jsonNode.get(key))));
            return pipelines.toArray(new Pipeline[pipelines.size()]);
        } catch (Exception e) {
            return new Pipeline[0];
        }
    }

    @Override
    public Template[] templates() {
        try {
            String content = http().get("/_template");
            JsonNode jsonNode = toJson(content);
            List<Template> templates = new ArrayList<>();
            jsonNode.fieldNames().forEachRemaining(key -> templates.add(new Template(key, jsonNode.get(key))));
            return templates.toArray(new Template[templates.size()]);
        } catch (Exception e) {
            return new Template[0];
        }
    }

    @Override
    public Response putPipeline(Pipeline pipeline) {
        try {
            http().put("/_ingest/pipeline/" + pipeline.getName(), pipeline.getJson().toString());
        } catch (Exception e) {
            return Response.failure(e.getMessage());
        }
        return Response.OK;
    }

    @Override
    public Response putTemplate(Template template) {
        try {
            http().put("/_template/" + template.getName(), template.getJson().toString());
        } catch (Exception e) {
            return Response.failure(e.getMessage());
        }
        return Response.OK;
    }

    private static String buildBulkRequest(Iterable<? extends ScrollResponse.Hit> docs) {
        StringBuffer body = new StringBuffer();
        for (ScrollResponse.Hit doc : docs) {
            if (Strings.isEmpty(doc.getSource())) {
                continue;
            }
            Map<String, String> meta = new LinkedHashMap();
            meta.put("_index", doc.getIndex());
            meta.put("_type", doc.getType());
            meta.put("_id", doc.getId());
            if (doc.getParent() != null) {
                meta.put("_parent", doc.getParent());
            }
            if (doc.getRouting() != null) {
                meta.put("_routing", doc.getRouting());
            }
            body.append(Json.toString(Map.of("index", meta))).append("\n");
            body.append(doc.getSource()).append("\n");
        }
        return body.toString();
    }
}
