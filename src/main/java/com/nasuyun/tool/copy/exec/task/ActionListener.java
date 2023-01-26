package com.nasuyun.tool.copy.exec.task;

import java.util.function.Consumer;

public interface ActionListener<Response> {

    void onResponse(Response response);

    void onFailure(Exception e);

    static <Response> ActionListener<Response> wrap(CheckedConsumer<Response, ? extends Exception> onResponse,
                                                    Consumer<Exception> onFailure) {
        return new ActionListener<Response>() {
            @Override
            public void onResponse(Response response) {
                try {
                    onResponse.accept(response);
                } catch (Exception e) {
                    onFailure(e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                onFailure.accept(e);
            }
        };
    }

    ActionListener EMPTY = wrap(o -> {
    }, e -> {
    });

}
