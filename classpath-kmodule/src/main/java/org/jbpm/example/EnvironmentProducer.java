package org.jbpm.example;

import java.io.File;
import java.io.FilenameFilter;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jbpm.services.task.identity.JBossUserGroupCallbackImpl;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.internal.runtime.manager.context.EmptyContext;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;

public class EnvironmentProducer {
    
    private static EnvironmentProducer instance;

    protected PoolingDataSource pds;
    
    protected RuntimeManager manager;
    
    protected RuntimeEngine engine;
    
    private EnvironmentProducer() {
        setupPoolingDataSource();
        setupRuntimeManager();
    }
    
    public static EnvironmentProducer getInstance() {
        if (instance == null) {
            instance = new EnvironmentProducer();
        }
        return instance;
    }
    
    public void close() {
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

    public RuntimeManager getRuntimeManager() {
        return manager;
    }
    
    public RuntimeEngine getEngine() {
        return engine;
    }
    
    public KieSession getKieSession() {
        return engine.getKieSession();
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
        pds.getDriverProperties().put("url", "jdbc:h2:jbpm-db;MVCC=true");
        pds.getDriverProperties().put("driverClassName", "org.h2.Driver");

        pds.init();
    }

    private void setupRuntimeManager() {
        cleanupSingletonSessionId();
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jbpm.persistence.unit");
        RuntimeEnvironmentBuilder builder = RuntimeEnvironmentBuilder.Factory
                .get()
                .newClasspathKmoduleDefaultBuilder()
                .entityManagerFactory(emf)
                .addEnvironmentEntry(EnvironmentName.TRANSACTION_MANAGER,
                        TransactionManagerServices.getTransactionManager())
                .userGroupCallback(new JBossUserGroupCallbackImpl("classpath:/usergroups.properties"));

        manager = RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(builder.get());

        engine = manager.getRuntimeEngine(EmptyContext.get());
    }
    
    private static void cleanupSingletonSessionId() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        if (tempDir.exists()) {
            
            String[] jbpmSerFiles = tempDir.list(new FilenameFilter() {
                
                public boolean accept(File dir, String name) {
                    
                    return name.endsWith("-jbpmSessionId.ser");
                }
            });
            for (String file : jbpmSerFiles) {
                
                new File(tempDir, file).delete();
            }
        }
    }
    
}
