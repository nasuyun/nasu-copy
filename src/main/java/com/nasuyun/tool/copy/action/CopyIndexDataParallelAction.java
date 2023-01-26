package com.nasuyun.tool.copy.action;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.nasuyun.tool.copy.action.State.Status;
import com.nasuyun.tool.copy.core.api.Cluster;
import com.nasuyun.tool.copy.core.api.Response;
import com.nasuyun.tool.copy.core.api.ScrollResponse;
import com.nasuyun.tool.copy.exec.Config;
import com.nasuyun.tool.copy.exec.PeerClusterRequest;
import com.nasuyun.tool.copy.exec.task.ThreadPool;
import com.nasuyun.tool.copy.utils.ByteSizeValue;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.LongAdder;

import static com.nasuyun.tool.copy.action.Global.globalDataReaderBuffer;
import static com.nasuyun.tool.copy.utils.Simplify.sleepMillis;

/**
 * 并行写入
 */
@Slf4j
@Getter
public class CopyIndexDataParallelAction implements Action {

    private final PeerClusterRequest request;
    private final Cluster source;
    private final Cluster dest;
    private final String index;
    private final ThreadPool threadPool;
    private final Config config;

    private final Metric metric;
    private final CopyDataState state;

    private volatile boolean cancelled;
    private LongAdder processNumbers;

    public CopyIndexDataParallelAction(PeerClusterRequest request, String index, ThreadPool threadPool, Config config) {
        this.request = request;
        this.source = request.sourceCluster();
        this.dest = request.destCluster();
        this.index = index;
        this.threadPool = threadPool;
        this.config = config;
        this.cancelled = false;
        this.processNumbers = new LongAdder();
        long totals = this.source.count(index);
        this.metric = new Metric(config.getMetricRegistry(), index, totals);
        this.state = new CopyDataState(metric);
    }

    @Override
    public String type() {
        return "copy-index-data-async";
    }

    @Override
    public State state() {
        return state;
    }

    @Override
    public void cancel() {
        this.cancelled = true;
    }

    @Override
    public Boolean execute() {
        state.status = Status.RUNNING;
        long startTime = System.currentTimeMillis();
        ScrollResponse scrollResponse = source.scroll(index, "5m", config.getScrollQuerySize());
        globalDataReaderBuffer.add(scrollResponse.bytes());
        metric.incrementQueryTime(spendMillis(startTime));

        while (scrollResponse.isEmpty() == false && cancelled == false) {
            if (globalDataReaderBuffer.longValue() > config.getReaderBufferSize()) {
                // 当读数据缓存区满时等待
                sleepMillis(config.getReadWaitMillis());
                continue;
            }

            // 异步处理bulk
            processNumbers.increment();
            bulkingAsync(scrollResponse);

            // scroll next
            long currentTime = System.currentTimeMillis();
            scrollResponse = source.scrollNext(index, scrollResponse.getScrollId(), config.getScrollQuerySize());
            globalDataReaderBuffer.add(scrollResponse.bytes());
            metric.incrementQueryTime(spendMillis(currentTime));
        }

        while (processNumbers.longValue() > 0) {
            sleepMillis(2000);
            log.debug(" wait bulk batch:[{}] [{}]", index, processNumbers);
        }

        Response updateReplicas = dest.replicas(index, 1);
        state.status = Status.COMPLETED;
        log.debug("update bulked index [{}] replicas [{}]", updateReplicas, 1);
        long procssTimeInMillis = System.currentTimeMillis() - startTime;
        log.info("[{}] copy finished , spend time [{}s]", index, (float) (procssTimeInMillis / 1000));
        return true;
    }

    void bulkingAsync(final ScrollResponse scrollResponse) {
        threadPool.bulk(() -> {
            if (cancelled == false) {
                long currentTime = System.currentTimeMillis();
                Response response;
                try {
                    response = dest.bulk(scrollResponse);
                    if (response.isSuccess()) {
                        metric.processed(scrollResponse);
                        metric.incrementIndexingTime(spendMillis(currentTime));
                    } else {
                        // TODO retry
                        log.warn("bulk error {}", response.getMessage());
                    }
                } finally {
                    globalDataReaderBuffer.add(-scrollResponse.bytes());
                    processNumbers.decrement();
                }
            }
        });
    }

    private static long spendMillis(long start) {
        return System.currentTimeMillis() - start;
    }


    public static class CopyDataState implements State {

        @Getter
        private String index;
        @Getter
        private volatile Status status = Status.WAIT;
        private Metric metric;

        public CopyDataState(Metric metric) {
            this.metric = metric;
            this.index = metric.index;
        }

        public MetricSnapshot getMetric() {
            return MetricSnapshot.of(metric);
        }

        @Override
        public String getMessage() {
            return null;
        }

        @Override
        public Status getStatus() {
            return status;
        }

        @Data
        public static class MetricSnapshot {
            private long totals;
            private long indexingTimeInMillis;
            private long queryTimeInMillis;
            private long processBytesCount;
            private String processBytesMeanRate;
            private String processBytesOneMinuteRate;
            private long processDocsCount;
            private String processDocsMeanRate;
            private String processDocsOneMinuteRate;

            public static MetricSnapshot of(Metric metric) {
                MetricSnapshot snapshot = new MetricSnapshot();
                snapshot.setTotals(metric.getTotals());
                snapshot.setIndexingTimeInMillis(metric.getIndexingTimeInMillis().getCount());
                snapshot.setQueryTimeInMillis(metric.getQueryTimeInMillis().getCount());
                snapshot.setProcessBytesCount(metric.getProcessBytes().getCount());
                snapshot.setProcessBytesMeanRate(byteRate(metric.getProcessBytes().getMeanRate()));
                snapshot.setProcessBytesOneMinuteRate(byteRate(metric.getProcessBytes().getOneMinuteRate()));
                snapshot.setProcessDocsCount(metric.getProcessDocs().getCount());
                snapshot.setProcessDocsMeanRate(docRate(metric.getProcessDocs().getMeanRate()));
                snapshot.setProcessDocsOneMinuteRate(docRate(metric.getProcessDocs().getOneMinuteRate()));
                return snapshot;
            }
        }

    }

    // 拷贝状态
    @Data
    public static class Metric {
        // 索引
        private final String index;
        // 文档总数
        @Setter
        private final long totals;
        // 写入总耗时
        private final Counter indexingTimeInMillis;
        // 查询总耗时
        private final Counter queryTimeInMillis;
        // 处理数据量速率
        private final Meter processBytes;
        // 处理文档速率
        private final Meter processDocs;

        public Metric(MetricRegistry metricRegistry, String index, long totals) {
            this.index = index;
            this.totals = totals;
            this.indexingTimeInMillis = metricRegistry.counter("indexingTimeInMillis-" + index);
            this.queryTimeInMillis = metricRegistry.counter("queryTimeInMillis-" + index);
            this.processBytes = metricRegistry.meter("processBytes-" + index);
            this.processDocs = metricRegistry.meter("processDocs-" + index);
        }

        public void processed(ScrollResponse scrollResponse) {
            processBytes.mark(scrollResponse.bytes());
            processDocs.mark(scrollResponse.getHits().size());
        }

        public void incrementQueryTime(long millis) {
            queryTimeInMillis.inc(millis);
        }

        public void incrementIndexingTime(long millis) {
            indexingTimeInMillis.inc(millis);
        }
    }

    private static String byteRate(double value) {
        long v = ((Double) value).longValue();
        return new ByteSizeValue(v) + "/s";
    }

    private static String docRate(double value) {
        long v = ((Double) value).longValue();
        return v + "/s";
    }

}