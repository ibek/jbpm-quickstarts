package org.jbpm.example.scenario;

import org.jbpm.example.EnvironmentProducer;
import org.kie.api.runtime.KieSession;

/**
 * A simple scenario to start a process with a script task which prints Hello
 * World message in the console.
 */
public class HelloWorld {

    public static void main(String[] args) {
        EnvironmentProducer env = EnvironmentProducer.getInstance();
        KieSession ksession = env.getKieSession();
        ksession.startProcess("org.jbpm.example.HelloWorldProcess");
        env.close();
    }

}