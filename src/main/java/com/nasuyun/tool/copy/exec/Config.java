package com.nasuyun.tool.copy.exec;

import com.codahale.metrics.MetricRegistry;
import com.nasuyun.tool.copy.utils.ByteSizeValue;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Config {

    @Getter
    @Setter
    private int scrollQuerySize = 1000;

    // 读取的数据阈值
    @Value("${read.buffer:256mb}")
    private String readBufferSize;

    @Value("${read.wait.millis:5000}")
    private long readWaitMillis;

    @Getter
    private final MetricRegistry metricRegistry = new MetricRegistry();

    public long getReaderBufferSize() {
        return ByteSizeValue.parseBytesSizeValue(readBufferSize).getBytes();
    }

    public long getReadWaitMillis() {
        return readWaitMillis;
    }

    public int getScrollQuerySize() {
        int v = Math.max(10, Math.min(scrollQuerySize, 10000));
        return v;
    }

}
