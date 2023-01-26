package com.nasuyun.tool.copy.action;

import com.nasuyun.tool.copy.core.api.Cluster;
import com.nasuyun.tool.copy.core.api.Response;
import com.nasuyun.tool.copy.core.model.Template;

public class CopyTemplateAction implements Action {

    public static final String COPY_INDEX_METAD_ACTION = "copy-template";

    private final Cluster destCluster;
    private final Template sourceTemplate;
    private final StateBase state;

    @Override
    public String type() {
        return COPY_INDEX_METAD_ACTION;
    }

    public CopyTemplateAction(Cluster destCluster, Template sourceTemplate) {
        this.destCluster = destCluster;
        this.sourceTemplate = sourceTemplate;
        this.state = new StateBase();
    }

    @Override
    public Boolean execute() {
        state.status(State.Status.RUNNING);
        Response response = destCluster.putTemplate(sourceTemplate);
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
