package com.redhat.lot.poc.fixacceptor;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import io.quarkus.runtime.StartupEvent;
import quickfix.ConfigError;
import quickfix.ThreadedSocketAcceptor;

public class AppLogonListener {

    @Inject
    Logger log; 

    @Inject
    ThreadedSocketAcceptor threadedSocketAcceptor;

    void onStart(@Observes StartupEvent ev) {               
        log.info("The application is starting...");
        startFixAcceptor();
    }

    private void startFixAcceptor (){
            try {
            	threadedSocketAcceptor.start();
                log.info("--------- ThreadedSocketAcceptor started ---------");
            } catch (ConfigError configError) {
                configError.printStackTrace();
                log.error("--------- ThreadedSocketAcceptor ran into an error ---------");
            }
    }

}
