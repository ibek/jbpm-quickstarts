package org.jbpm.example.scenario;

import java.util.List;

import org.jbpm.example.EnvironmentProducer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;

/**
 * A simple scenario to start a process with a single human task. After the task
 * is done, we get a notification in the console from a script task.
 * 
 * Reproducer for BZ 1094798
 */
public class SingleHumanTask {

    public static void main(String[] args) {
        EnvironmentProducer env = EnvironmentProducer.getInstance();
        KieSession ksession = env.getKieSession();
        ProcessInstance pi = ksession.startProcess("org.jbpm.example.SingleHumanTaskProcess");

        TaskService taskService = env.getEngine().getTaskService();
        List<Long> tasks = taskService.getTasksByProcessInstanceId(pi.getId());
        Long taskId = tasks.get(0); // there is only one task active

        taskService.start(taskId, "mary");
        taskService.complete(taskId, "mary", null);

        env.close();
    }

}
