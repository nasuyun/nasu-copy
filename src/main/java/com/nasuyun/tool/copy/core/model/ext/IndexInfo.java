package com.nasuyun.tool.copy.core.model.ext;

import com.fasterxml.jackson.databind.JsonNode;
import com.nasuyun.tool.copy.core.model.IndexMeta;
import lombok.Getter;

import static com.nasuyun.tool.copy.utils.Json.firstNode;

@Getter
public class IndexInfo {

    private boolean sourceEnabled = true;

    public static IndexInfo of(IndexMeta indexMeta) {
        IndexInfo indexInfo = new IndexInfo();
        JsonNode mappings = indexMeta.getMappings().getJson();
        JsonNode type = firstNode(mappings);
        if (type != null) {
            JsonNode source = type.get("_source");
            if (source != null) {
                boolean enabled = source.get("enabled").asBoolean();
                indexInfo.sourceEnabled = enabled;
            }
            JsonNode properties = type.get("properties");
            if (properties != null) {
                properties.fieldNames().forEachRemaining(fieldName -> {
                    JsonNode fieldInfo = properties.get(fieldName);
                    String fieldType = fieldInfo.get("type").textValue();
                    // doc_values store_fields 检查
                });
            }
        }
        return indexInfo;
    }

}
