package com.nasuyun.tool.copy.exec;

import com.nasuyun.tool.copy.action.CopyPipelineAction;
import com.nasuyun.tool.copy.core.api.Cluster;
import com.nasuyun.tool.copy.core.model.Pipeline;
import com.nasuyun.tool.copy.exec.task.TaskManager;
import com.nasuyun.tool.copy.utils.Regex;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CopyPipeline {

    @Autowired
    private TaskManager taskManager;

    public List<String> copyPipeline(PeerClusterRequest request, String pipeline) {
        List<String> copyed = new ArrayList<>();
        Cluster source = request.sourceCluster();
        Cluster dest = request.destCluster();
        String pattern = Strings.isNotEmpty(pipeline) ? pipeline : "*";
        Pipeline[] sourcePipelines = source.pipelines();
        if (sourcePipelines == null || sourcePipelines.length == 0) {
            log.info("source pipelines is empty, finish him .");
            return copyed;
        }
        List<Pipeline> destPipelines = Arrays.stream(sourcePipelines).filter(v -> Regex.simpleMatch(pattern, v.getName())).collect(Collectors.toList());

        for (Pipeline sourcePipeline : destPipelines) {
            CopyPipelineAction action = new CopyPipelineAction(dest, sourcePipeline);
            copyed.add(sourcePipeline.getName());
            taskManager.createTask("copy-pipeline:" + sourcePipeline.getName(), action);
        }
        return copyed;
    }
}
