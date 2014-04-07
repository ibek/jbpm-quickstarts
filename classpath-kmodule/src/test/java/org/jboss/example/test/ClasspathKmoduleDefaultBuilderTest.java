package org.jboss.example.test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jbpm.services.task.identity.JBossUserGroupCallbackImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.manager.context.EmptyContext;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;

public class ClasspathKmoduleDefaultBuilderTest {

    protected PoolingDataSource pds;

    protected RuntimeManager manager;

    protected RuntimeEngine engine;

    @Before
    public void setup() {
        setupPoolingDataSource();
        setupRuntimeManager();
    }

    @After
    public void cleanup() {
        if (manager != null) {
            manager.close();
            manager = null;
        }
        if (pds != null) {
            pds.close();
            pds = null;
        }
        System.clearProperty("java.naming.factory.initial");
    }

    @Test
    public void testHelloWorldProcess() {
        KieSession ksession = engine.getKieSession();
        ProcessInstance pi = ksession.startProcess("org.jboss.example.HelloWorldProcess");
        Assert.assertNotNull(pi);
        Assert.assertEquals(ProcessInstance.STATE_COMPLETED, pi.getState());
    }

    private void setupPoolingDataSource() {
        System.setProperty("java.naming.factory.initial", "bitronix.tm.jndi.BitronixInitialContextFactory");
        pds = new PoolingDataSource();
        pds.setUniqueName("jdbc/jbpm-ds");
        pds.setMaxPoolSize(5);
        pds.setAllowLocalTransactions(true);

        pds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");

        pds.getDriverProperties().put("user", "sa");
        pds.getDriverProperties().put("password", "");
        pds.getDriverProperties().put("url", "jdbc:h2:inmemory:jbpm-db;MVCC=true");
        pds.getDriverProperties().put("driverClassName", "org.h2.Driver");

        pds.init();
    }

    private void setupRuntimeManager() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jbpm.persistence.unit");
        RuntimeEnvironmentBuilder builder = RuntimeEnvironmentBuilder.Factory
                .get()
                .newClasspathKmoduleDefaultBuilder()
                .entityManagerFactory(emf)
                .addEnvironmentEntry(EnvironmentName.TRANSACTION_MANAGER,
                        TransactionManagerServices.getTransactionManager())
                .userGroupCallback(new JBossUserGroupCallbackImpl("classpath:/usergroups.properties"));

        manager = RuntimeManagerFactory.Factory.get().newPerProcessInstanceRuntimeManager(builder.get());

        engine = manager.getRuntimeEngine(EmptyContext.get());
    }

}
