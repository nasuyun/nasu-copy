package com.nasuyun.tool.copy.action;

import com.nasuyun.tool.copy.core.api.Cluster;
import com.nasuyun.tool.copy.core.api.Response;
import com.nasuyun.tool.copy.core.model.Pipeline;

public class CopyPipelineAction implements Action {

    public static final String COPY_INDEX_METAD_ACTION = "copy-pipeline";

    private final Pipeline sourcePipeline;
    private final Cluster destCluster;
    private StateBase state;

    @Override
    public String type() {
        return COPY_INDEX_METAD_ACTION;
    }

    public CopyPipelineAction(Cluster destCluster, Pipeline sourcePipeline) {
        this.destCluster = destCluster;
        this.sourcePipeline = sourcePipeline;
        this.state = new StateBase();
    }

    @Override
    public Boolean execute() {
        state.status(State.Status.RUNNING);
        Response response = destCluster.putPipeline(sourcePipeline);
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
