package com.nasuyun.tool.copy.exec;

import com.nasuyun.tool.copy.core.api.Cluster;
import com.nasuyun.tool.copy.core.api.ClusterFactory;
import com.nasuyun.tool.copy.core.api.ConnectInfo;
import lombok.Data;
import org.springframework.util.Assert;

@Data
public class PeerClusterRequest {
    private ConnectInfo source;
    private ConnectInfo dest;

    public PeerClusterRequest() {

    }

    public PeerClusterRequest(ConnectInfo source, ConnectInfo dest) {
        this.source = source;
        this.dest = dest;
    }

    public Cluster sourceCluster() {
        Cluster cluster = ClusterFactory.factory(source);
        Assert.notNull(cluster, "source cluster not connected");
        return cluster;
    }

    public Cluster destCluster() {
        Cluster cluster = ClusterFactory.factory(dest);
        Assert.notNull(cluster, "target cluster not connected");
        return cluster;
    }
}
