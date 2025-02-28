package com.linkedpipes.etl.executor.api.v1.component.task;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.report.ReportWriter;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO Add test for failing task.
 */
public class TaskExecutionTest {

    private static class MockedTask implements Task {

        @Override
        public String getIri() {
            return "http://task";
        }
    }

    private static class MockedTaskExecution extends TaskExecution<Task> {

        TaskExecutionConfiguration config =
                Mockito.mock(TaskExecutionConfiguration.class);

        List<Task> tasks = new ArrayList<>();

        List<TaskConsumer> consumers = new ArrayList<>();

        ReportWriter report = Mockito.mock(ReportWriter.class);

        @Override
        protected TaskExecutionConfiguration getExecutionConfiguration() {
            return this.config;
        }

        @Override
        protected TaskSource<Task> createTaskSource() throws LpException {
            return TaskSource.defaultTaskSource(tasks);
        }

        @Override
        protected TaskConsumer<Task> createConsumer() {
            TaskConsumer consumer = Mockito.mock(TaskConsumer.class);
            consumers.add(consumer);
            return consumer;
        }

        @Override
        protected ReportWriter createReportWriter() {
            return report;
        }
    }

    @BeforeClass
    public static void beforeClass() {
        MDC.setContextMap(Collections.EMPTY_MAP);
    }

    @Test
    public void executeOneConsumer() throws LpException {
        MockedTaskExecution component = new MockedTaskExecution();
        Mockito.when(component.config.getThreadsNumber()).thenReturn(1);
        Mockito.when(component.config.isSkipOnError()).thenReturn(false);
        component.tasks.add(new MockedTask());
        component.tasks.add(new MockedTask());
        component.execute();

        Assert.assertEquals(1, component.consumers.size());
        TaskConsumer<Task> consumer = component.consumers.get(0);
        Mockito.verify(consumer, Mockito.times(2)).accept(Mockito.any());
        Mockito.verify(component.report, Mockito.times(2)).onTaskFinished(
                Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void executeTwoConsumers() throws LpException {
        MockedTaskExecution component = new MockedTaskExecution();
        Mockito.when(component.config.getThreadsNumber()).thenReturn(2);
        Mockito.when(component.config.isSkipOnError()).thenReturn(false);
        component.tasks.add(new MockedTask());
        component.tasks.add(new MockedTask());
        component.execute();

        Assert.assertEquals(2, component.consumers.size());
        Mockito.verify(component.report, Mockito.times(2)).onTaskFinished(
                Mockito.any(), Mockito.any(), Mockito.any());
    }

}
