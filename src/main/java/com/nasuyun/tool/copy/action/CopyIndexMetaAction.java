package com.nasuyun.tool.copy.action;

import com.nasuyun.tool.copy.core.api.Cluster;
import com.nasuyun.tool.copy.core.api.Response;
import com.nasuyun.tool.copy.core.model.IndexMeta;
import com.nasuyun.tool.copy.exec.PeerClusterRequest;

/**
 * 拷贝单个索引元数据
 */
public class CopyIndexMetaAction implements Action {

    private final PeerClusterRequest request;
    private final String index;
    private final StateBase state;

    @Override
    public String type() {
        return "copy-index-meta";
    }

    public CopyIndexMetaAction(PeerClusterRequest request, String index) {
        this.request = request;
        this.index = index;
        this.state = new StateBase();
    }

    @Override
    public Boolean execute() {
        state.status(State.Status.RUNNING);
        Cluster source = request.sourceCluster();
        Cluster target = request.destCluster();
        IndexMeta indexMeta = source.get(index);
        Response response = target.create(indexMeta);
        if (response.isSuccess()) {
            state.status(State.Status.COMPLETED);
        } else {
            state.failure(response.getMessage());
        }
        return true;
    }

    @Override
    public State state() {
        return state;
    }

}
