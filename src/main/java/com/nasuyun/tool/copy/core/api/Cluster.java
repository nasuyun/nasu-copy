package com.nasuyun.tool.copy.core.api;

import com.nasuyun.tool.copy.core.model.IndexMeta;
import com.nasuyun.tool.copy.core.model.Pipeline;
import com.nasuyun.tool.copy.core.model.Template;
import com.nasuyun.tool.copy.core.model.Version;
import lombok.Data;

public interface Cluster {

    Version version();

    HealthResponse health();

    IndexInfo[] indices();

    IndexMeta get(String index);

    Response create(IndexMeta indexMeta);

    Response delete(String index);

    ScrollResponse scroll(String index, String time, int batchSize);

    ScrollResponse scrollNext(String index, String scrollId, int batchSize);

    Response bulk(ScrollResponse response);

    long count(String index);

    Response replicas(String index, int replicas);

    Pipeline[] pipelines();

    Template[] templates();

    Response putPipeline(Pipeline pipeline);

    Response putTemplate(Template template);

    @Data
    class IndexInfo {
        String index;
        String status;
        String health;
        String docs;
        String size;
    }
}
