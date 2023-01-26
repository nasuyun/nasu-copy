package com.nasuyun.tool.copy.exec.task;

import com.nasuyun.tool.copy.action.Action;
import com.nasuyun.tool.copy.action.State;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaskManager {

    @Autowired
    private ThreadPool threadPool;

    private Map<String, Task> tasks = new ConcurrentHashMap<>();

    private List<Task> historyTasks = new ArrayList<>();

    public void management(Runnable runnable) {
        threadPool.management(runnable);
    }

    public boolean createTask(String name, Action action) {
        return createTask(name, action, ActionListener.wrap(o -> {
        }, e -> log.error("", e)));
    }

    public boolean createTask(String name, Action action, ActionListener listener) {
        if (tasks.containsKey(name)) {
            return false;
        }
        Task task = new Task(name, action);
        tasks.put(name, task);
        log.debug("task start {}", task.getName());
        threadPool.generic(
                () -> {
                    try {
                        Object response = task.executeAction();
                        listener.onResponse(response);
                    } catch (Exception e) {
                        listener.onFailure(e);
                        task.setMessage(e.getMessage());
                        log.error("task failed [{}] ", name, e);
                    } finally {
                        log.info("task finished [{}]", name);
                        task.setMessage(task.action().state().getMessage());
                        tasks.remove(name);
                        task.setStopTime(new Date());
                        historyTasks.add(task);
                    }
                }
        );
        return true;
    }

    public boolean cancel(String name) {
        Task task = tasks.get(name);
        if (task != null) {
            task.cancel();
            return true;
        }
        return false;
    }


    public List<Task> getHistoryTasks() {
        historyTasks.sort(Comparator.comparing(Task::getStartTime).reversed());
        return historyTasks;
    }

    public boolean cleanHistoryTasks() {
        historyTasks = new ArrayList<>();
        return true;
    }

    public Object state() {
        return tasks.values();
    }

    // 根据状态过滤
    public Object state(String statusFilted) {
        return tasks.values().stream()
                .filter(v -> State.Status.of(statusFilted).equals(v.getState().getStatus()))
                .collect(Collectors.toList());
    }

}
