package com.nasuyun.tool.copy.core.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@ToString
public class ScrollResponse {

    @Getter
    private String scrollId;
    @Getter
    private List<Hit> hits;

    private ArrayNode hitsNode;

    public ScrollResponse(String scrollId, ArrayNode hitsNode) {
        this.scrollId = scrollId;
        this.hitsNode = hitsNode;
        if (hitsNode != null && hitsNode.isEmpty() == false) {
            List<Hit> parsedHits = new ArrayList<>();
            for (JsonNode jsonNode : hitsNode) {
                parsedHits.add(Hit.of(jsonNode));
            }
            this.hits = parsedHits;
        }
    }

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(hits);
    }

    public long docs() {
        return isEmpty() ? 0 : hits.size();
    }

    public long bytes() {
        if (isEmpty()) {
            return 0;
        } else {
            long bytes = 0;
            for (Hit hit : hits) {
                bytes += hit.bytes();
            }
            return bytes;
        }
    }

    @Data
    public static class Hit {
        private String index;
        private String type;
        private String id;
        private String source;
        private String parent;
        private String routing;

        public long bytes() {
            return source.length();
        }

        public static Hit of(JsonNode jsonNode) {
            Hit hit = new Hit();
            hit.setIndex(jsonNode.get("_index").asText());
            hit.setType(jsonNode.get("_type").asText());
            hit.setId(jsonNode.get("_id").asText());
            if (jsonNode.has("_source")) {
                hit.setSource(jsonNode.get("_source").toString());
            }
            if (jsonNode.has("_parent")) {
                hit.setParent(jsonNode.get("_parent").asText());
            }
            if (jsonNode.has("_routing")) {
                hit.setRouting(jsonNode.get("_routing").asText());
            }
            return hit;
        }
    }

}
