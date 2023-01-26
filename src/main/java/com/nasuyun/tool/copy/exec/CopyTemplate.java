package com.nasuyun.tool.copy.exec;

import com.nasuyun.tool.copy.action.CopyTemplateAction;
import com.nasuyun.tool.copy.core.api.Cluster;
import com.nasuyun.tool.copy.core.model.Template;
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
public class CopyTemplate {

    @Autowired
    private TaskManager taskManager;

    public List<String> copyTemplate(PeerClusterRequest request, String template) {
        List<String> copyed = new ArrayList<>();
        Cluster source = request.sourceCluster();
        Cluster dest = request.destCluster();
        String pattern = Strings.isNotEmpty(template) ? template : "*";
        Template[] templates = source.templates();
        if (templates == null || templates.length == 0) {
            log.info("source pipelines is empty, finish him .");
            return copyed;
        }
        List<Template> destPipelines = Arrays.stream(templates).filter(v -> Regex.simpleMatch(pattern, v.getName())).collect(Collectors.toList());

        for (Template sourceTemplate : destPipelines) {
            CopyTemplateAction action = new CopyTemplateAction(dest, sourceTemplate);
            copyed.add(sourceTemplate.getName());
            taskManager.createTask("copy-template:" + sourceTemplate.getName(), action);
        }
        return copyed;
    }
}
