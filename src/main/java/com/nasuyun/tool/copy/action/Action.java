package com.nasuyun.tool.copy.action;

/**
 * 执行动作(同步方式）
 *
 * @param <Response>
 */
public interface Action<Response> extends Cancelable {

    /**
     * Action名称
     */
    String type();

    /**
     * 同步执行
     */
    Response execute();

    /**
     * 状态
     */
    State state();
}
