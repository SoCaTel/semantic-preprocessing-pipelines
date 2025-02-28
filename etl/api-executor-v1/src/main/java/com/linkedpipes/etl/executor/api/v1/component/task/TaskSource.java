package com.linkedpipes.etl.executor.api.v1.component.task;

import java.util.Collection;

public interface TaskSource<T extends Task> {

    /**
     * Should component ignore task failures.
     */
    void setSkipOnError(boolean endOnError);

    /**
     * Null if there is no Task to execute.
     */
    T getTask();

    void onTaskFinished(T task);

    void onTaskFailed(T task);

    boolean doesTaskFailed();

    static <T extends Task> TaskSource<T> defaultTaskSource(
            Collection<T> tasks) {
        return new SimpleTaskSource<>(tasks);
    }

    static <T extends GroupTask> TaskSource<T> groupTaskSource(
            Collection<T> tasks, int maxRunningInGroup) {
        return new GroupTaskSource<>(tasks, maxRunningInGroup);
    }

}
