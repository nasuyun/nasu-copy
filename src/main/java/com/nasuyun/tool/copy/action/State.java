package com.nasuyun.tool.copy.action;

import java.util.Arrays;
import java.util.Optional;

public interface State {

    String getMessage();

    Status getStatus();

    enum Status {
        WAIT("wait"), RUNNING("running"), COMPLETED("completed"), FAILED("failed"), UNKNOW("unkonw");
        private String value;

        Status(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        public static Status of(String value) {
            Optional<Status> first = Arrays.stream(values())
                    .filter(status -> status.value.equalsIgnoreCase(value))
                    .findFirst();
            return first.isPresent() ? first.get() : UNKNOW;
        }
    }

}
