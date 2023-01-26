package com.nasuyun.tool.copy.rest;

import com.nasuyun.tool.copy.core.api.Cluster;
import com.nasuyun.tool.copy.core.api.ClusterFactory;
import com.nasuyun.tool.copy.core.api.ConnectInfo;
import com.nasuyun.tool.copy.core.api.HealthResponse;
import com.nasuyun.tool.copy.exec.*;
import com.nasuyun.tool.copy.exec.task.ActionListener;
import com.nasuyun.tool.copy.exec.task.TaskManager;
import com.nasuyun.tool.copy.utils.Json;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.http.HttpRequest;
import java.util.Map;

import static com.nasuyun.tool.copy.utils.Simplify.sleepMillis;

@RestController
@CrossOrigin
@Slf4j
public class CopyController {

    @Autowired
    CopyData copyData;

    @Autowired
    CopyMeta copyMeta;

    @Autowired
    CopyIndex copyIndex;

    @Autowired
    CopyPipeline copyPipeline;

    @Autowired
    CopyTemplate copyTemplate;

    @Autowired
    TaskManager taskManager;

    @Autowired
    Config config;

    @Autowired
    SessionProvider sessionProvider;


    @RequestMapping(value = "/_test", method = RequestMethod.GET)
    public Object test() {
        return true;
    }

    @RequestMapping(value = "/api/login", method = RequestMethod.POST)
    public Object login(@RequestBody ConnectInfo connectInfo) throws HttpResponseException {
        Cluster cluster;
        try {
            cluster = ClusterFactory.factory(connectInfo);
            cluster.health();
            SessionProvider.UserCredential userCredential = sessionProvider.createSession(connectInfo.getUsername(), "admin");
            return Map.of("connected", true, "username", connectInfo.getUsername(), "sessionKey", userCredential.getSessionKey());
        } catch (Exception e) {
            throw new HttpResponseException(HttpStatus.UNAUTHORIZED.value(), "登录失败");
        }
    }

    @RequestMapping(value = "/api/currentUser")
    public Object current() {
        return sessionProvider.current();
    }

    @RequestMapping(value = "/api/test", method = RequestMethod.POST)
    public Object test(@RequestBody ConnectInfo connectInfo) {
        Cluster cluster;
        try {
            cluster = ClusterFactory.factory(connectInfo);
            HealthResponse healthResponse = cluster.health();
            return cluster != null ? Map.of(
                    "connected", true,
                    "health", healthResponse == null ? Map.of("cluster_name", "Nasu Elasticsearch Serverless") : healthResponse.getJson(),
                    "version", cluster.version().getVersion()) : Map.of("connected", false);
        } catch (Exception e) {
            return Map.of("connected", false);
        }
    }

    @RequestMapping(value = "/api/delete/{index}", method = RequestMethod.DELETE)
    public Object delete(@RequestBody ConnectInfo connectInfo, @PathVariable String index) {
        Cluster cluster = ClusterFactory.factory(connectInfo);
        return cluster.delete(index);
    }

    @RequestMapping(value = "/api/copy/meta/{index}", method = RequestMethod.POST)
    public Object copyIndex(
            @RequestBody PeerClusterRequest request,
            @PathVariable(required = false) String index) {
        copyMeta.copyMeta(request, index, ActionListener.EMPTY);
        return true;
    }

    @RequestMapping(value = "/api/copy/data/{index}", method = RequestMethod.POST)
    public Object copyData(
            @RequestBody PeerClusterRequest request,
            @PathVariable(required = false) String index) {
        copyData.copyData(request, index);
        return true;
    }

    @RequestMapping(value = "/api/copy/index/{index}", method = RequestMethod.POST)
    public Object copyIndexMeta(
            @RequestBody PeerClusterRequest request,
            @PathVariable(required = false) String index,
            @RequestParam(defaultValue = "true") boolean force) {
        copyIndex.copyIndex(request, index, force);
        return true;
    }

    @RequestMapping(value = "/api/copy/pipeline/{pipeline}", method = RequestMethod.POST)
    public Object copyPipeline(
            @RequestBody PeerClusterRequest request,
            @PathVariable(required = false) String pipeline
    ) {
        return copyPipeline.copyPipeline(request, pipeline);
    }

    @RequestMapping(value = "/api/copy/template/{template}", method = RequestMethod.POST)
    public Object copyTemplate(
            @RequestBody PeerClusterRequest request,
            @PathVariable(required = false) String template
    ) {
        return copyTemplate.copyTemplate(request, template);
    }

    @RequestMapping(value = "/api/indices", method = RequestMethod.POST)
    public Object indices(@RequestBody PeerClusterRequest request) {
        Cluster.IndexInfo[] sourceIndices = request.sourceCluster().indices();
        Cluster.IndexInfo[] destIndices = request.destCluster().indices();
        return Map.of(
                "sourceIndices", sourceIndices, "destIndices", destIndices
        );
    }

    @RequestMapping(value = "/api/state", method = RequestMethod.GET)
    public Object stateAll() {
        return taskManager.state();
    }

    @RequestMapping(value = "/api/state/{status}", method = RequestMethod.GET)
    public Object state(@PathVariable(required = false) String status) {
        return taskManager.state(status);
    }

    @RequestMapping(value = "/api/state/history", method = RequestMethod.GET)
    public Object stateHistory() {
        return taskManager.getHistoryTasks();
    }

    @RequestMapping(value = "/api/state/history", method = RequestMethod.DELETE)
    public Object cleanHistoryTasks() {
        return taskManager.cleanHistoryTasks();
    }


    @RequestMapping(value = "/api/cancel", method = RequestMethod.POST)
    public Object cancel(@RequestBody Map<String, String> params) {
        String taskName = params.get("taskName");
        return taskManager.cancel(taskName);
    }

    @RequestMapping(value = "/api/config", method = RequestMethod.POST)
    public Object config(@RequestBody Map<String, Object> params) {
        Object scrollQuerySize = params.get("scroll_query_size");
        if (scrollQuerySize != null) {
            this.config.setScrollQuerySize((Integer) scrollQuerySize);
        }
        return config;
    }
}
