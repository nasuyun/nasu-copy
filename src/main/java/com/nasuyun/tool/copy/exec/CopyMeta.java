package com.nasuyun.tool.copy.exec;

import com.nasuyun.tool.copy.action.CopyIndexMetaAction;
import com.nasuyun.tool.copy.core.api.Cluster;
import com.nasuyun.tool.copy.exec.task.ActionListener;
import com.nasuyun.tool.copy.exec.task.TaskManager;
import com.nasuyun.tool.copy.utils.Regex;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CopyMeta {

    @Autowired
    private TaskManager taskManager;

    public void copyMeta(PeerClusterRequest request, String pattern, ActionListener listener) {
        Cluster source = request.sourceCluster();
        String indexPattern = Strings.isNotEmpty(pattern) ? pattern : "*";

        Cluster.IndexInfo[] sourceIndices = source.indices();
        if (sourceIndices == null || sourceIndices.length == 0) {
            log.info("source indices is empty, finish him .");
            return;
        }
        List<String> indices = Arrays.stream(sourceIndices)
                .filter(v -> Regex.simpleMatch(indexPattern, v.getIndex()))
                .map(indexInfo -> indexInfo.getIndex())
                .collect(Collectors.toList());

        CountDownLatch countDownLatch = new CountDownLatch(indices.size());
        for (String index : indices) {
            CopyIndexMetaAction action = new CopyIndexMetaAction(request, index);
            taskManager.createTask("copy-index-meta:" + index, action, ActionListener.wrap(
                    r -> countDownLatch.countDown(), e -> countDownLatch.countDown()));
        }

        taskManager.management(() -> {
            try {
                countDownLatch.await(5, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                if (listener != null)
                    listener.onFailure(e);
            } finally {
                if (listener != null)
                    listener.onResponse("success");
            }
        });
    }
}
