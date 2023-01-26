package com.nasuyun.tool.copy.action;

import com.nasuyun.tool.copy.core.api.Cluster;
import com.nasuyun.tool.copy.core.api.Response;
import com.nasuyun.tool.copy.core.api.ScrollResponse;
import com.nasuyun.tool.copy.exec.Config;
import com.nasuyun.tool.copy.exec.PeerClusterRequest;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static com.nasuyun.tool.copy.utils.RateUtil.getRate;

/**
 * 拷贝单个索引数据
 */
@Slf4j
@Getter
public class CopyIndexDataAction implements Action {

    private final PeerClusterRequest request;
    private final Cluster source;
    private final Cluster dest;
    private final String index;
    private final CopyState state;
    private final Config config;

    private volatile boolean cancelled = false;

    public CopyIndexDataAction(PeerClusterRequest request, String index, Config config) {
        this.request = request;
        this.source = request.sourceCluster();
        this.dest = request.destCluster();
        this.index = index;
        this.config = config;
        this.state = new CopyState();
    }

    @Override
    public String type() {
        return "copy-index-data";
    }

    @Override
    public Boolean execute() {
        long docs = source.count(index);
        state.status = State.Status.RUNNING;
        state.index = index;
        state.totals += docs;
        log.debug("scroll first started: index[{}] batch[{}]", index, config.getScrollQuerySize());

        long currentTime = System.currentTimeMillis();
        ScrollResponse scrollResponse = source.scroll(index, "5m", config.getScrollQuerySize());
        state.queryTimeInMillis += System.currentTimeMillis() - currentTime;

        while (scrollResponse.isEmpty() == false) {
            if (cancelled) {
                break;
            }

            // 写入
            long currentIndexingTime = System.currentTimeMillis();
            Response response = dest.bulk(scrollResponse);
            state.indexingTimeInMillis += (System.currentTimeMillis() - currentIndexingTime);

            if (response.isSuccess()) {
                state.completed += scrollResponse.docs();
                state.indexingBytes += scrollResponse.bytes();
                // （读取+写入）耗时
                long onceInMillis = System.currentTimeMillis() - currentTime;
                state.rate = getRate(scrollResponse.bytes(), onceInMillis);
            } else {
                log.error("bulk failure found {}", response);
            }
            log.debug("scroll next: index[{}] batch[{}]", index, config.getScrollQuerySize());

            // 读取
            currentTime = System.currentTimeMillis();
            scrollResponse = source.scrollNext(index, scrollResponse.getScrollId(), config.getScrollQuerySize());
            state.queryTimeInMillis += (System.currentTimeMillis() - currentTime);
        }
        state.status = State.Status.COMPLETED;
        log.debug("scroll completed: index[{}]", index);
        return true;
    }

    @Override
    public State state() {
        return state;
    }

    @Override
    public void cancel() {
        this.cancelled = true;
    }

    // 拷贝状态
    @Data
    public static class CopyState implements State {
        private String index;
        private Status status = Status.WAIT;
        private long totals = 0;
        private long completed = 0;
        private long indexingBytes = 0;
        private long indexingTimeInMillis = 0;
        private long queryTimeInMillis = 0;
        private String rate;

        public CopyState merge(CopyState other) {
            this.totals += other.totals;
            this.completed += other.completed;
            this.indexingBytes += other.indexingBytes;
            this.indexingTimeInMillis += other.indexingTimeInMillis;
            this.queryTimeInMillis += other.queryTimeInMillis;
            return this;
        }

        @Override
        public String getMessage() {
            return null;
        }

        @Override
        public Status getStatus() {
            return status;
        }
    }

}