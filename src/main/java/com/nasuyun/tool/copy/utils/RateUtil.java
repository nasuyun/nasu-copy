package com.nasuyun.tool.copy.utils;

public class RateUtil {

    public static String getRate(long bytes, long millis) {
        if (millis < 1000) {
            return new ByteSizeValue(bytes) + "/" + millis + "ms";
        } else {
            return new ByteSizeValue(bytes / (millis / 1000)) + "/s";
        }
    }

    public static void main(String[] args) {
        System.out.println(getRate(1024, 100));
        System.out.println(getRate(1024, 1000));
        System.out.println(getRate(1024 * 1024, 1000));
        System.out.println(getRate(1024 * 1024, 2000));
    }
}
