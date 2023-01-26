package com.nasuyun.tool.copy.exec.task;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public
class ThreadPool {

    private ExecutorService management;
    private ExecutorService generic;
    private ExecutorService bulk;

    @Value("${thread_pool.bulk.size:10}")
    private int bulkPoolSize;

    @PostConstruct
    void onCreate() {
        management = Executors.newFixedThreadPool(1);
        generic = Executors.newCachedThreadPool();
        bulk = Executors.newFixedThreadPool(bulkPoolSize);
    }

    public void generic(Runnable runnable) {
        generic.submit(runnable);
    }

    public void management(Runnable runnable) {
        management.submit(runnable);
    }

    public void bulk(Runnable runnable) {
        bulk.submit(runnable);
    }

}
