package com.nasuyun.tool.copy.exec;

import com.nasuyun.tool.copy.exec.task.ActionListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 拷贝索引元数据及数据
 */
@Slf4j
@Component
public class CopyIndex {

    @Autowired
    CopyMeta copyMeta;

    @Autowired
    CopyData copyData;

    public void copyIndex(PeerClusterRequest request, String pattern, boolean force) {
        copyMeta.copyMeta(request, pattern, ActionListener.wrap(
                response -> {
                    copyData.copyData(request, pattern);
                },
                error -> {
                    if (force) {
                        copyData.copyData(request, pattern);
                    } else {
                        throw new RuntimeException(error);
                    }
                }));
    }
}
