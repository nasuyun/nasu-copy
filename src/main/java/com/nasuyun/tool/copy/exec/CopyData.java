package com.nasuyun.tool.copy.exec;

import com.nasuyun.tool.copy.action.*;
import com.nasuyun.tool.copy.core.api.Cluster;
import com.nasuyun.tool.copy.core.api.Cluster.IndexInfo;
import com.nasuyun.tool.copy.exec.task.TaskManager;
import com.nasuyun.tool.copy.exec.task.ThreadPool;
import com.nasuyun.tool.copy.utils.Regex;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CopyData {

    @Autowired
    private TaskManager taskManager;

    @Autowired
    private ThreadPool threadPool;

    @Autowired
    private Config config;

    public void copyData(PeerClusterRequest request, String pattern) {
        Cluster source = request.sourceCluster();
        String indexPattern = Strings.isNotEmpty(pattern) ? pattern : "*";

        IndexInfo[] sourceIndices = source.indices();
        if (sourceIndices == null || sourceIndices.length == 0) {
            log.info("source indices is empty, finish him .");
            return;
        }
        List<String> indices = Arrays.stream(sourceIndices)
                .filter(v -> Regex.simpleMatch(indexPattern, v.getIndex()))
                .map(indexInfo -> indexInfo.getIndex())
                .collect(Collectors.toList());
        for (String index : indices) {
            Action action = new CopyIndexDataParallelAction(request, index, threadPool, config);
            taskManager.createTask("copy-index-data:" + index, action);
        }
    }
}
