package com.nasuyun.tool.copy.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.function.Consumer;

@Slf4j
public class Simplify {

    /**
     * 忽略异常
     *
     * @param same
     */
    public static void ignoreExc(Executor same) {
        ignoreExc(same, e -> log.error("", e));
    }

    public static void ignoreExc(Executor same, Consumer<Exception> onError) {
        try {
            same.exec();
        } catch (Exception ex) {
            onError.accept(ex);
        }
    }

    public static <T> ImmutablePair<T, Long> timeSpendMillis(Callable<T> fun) {
        long current = System.currentTimeMillis();
        T result = fun.call();
        long spend = System.currentTimeMillis() - current;
        return ImmutablePair.of(result, spend);
    }

    public static void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    @FunctionalInterface
    public interface Executor {
        void exec() throws Exception;
    }

    @FunctionalInterface
    public interface Callable<V> {
        V call();
    }

}
