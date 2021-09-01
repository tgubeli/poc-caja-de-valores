package com.redhat.lot.poc.fixacceptor;

import quickfix.ConfigError;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.ThreadedSocketAcceptor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;

public class AppLogonListener {

    @Inject
    Logger log; 

    @Inject
    ThreadedSocketAcceptor threadedSocketAcceptor;

    private boolean acceptorStarted = false;

    void onStart(@Observes StartupEvent ev) {               
        log.info("The application is starting...");
        startFixAcceptor();
    }

    private void startFixAcceptor (){
        if(!acceptorStarted) {
            try {
            	threadedSocketAcceptor.start();
                log.info("--------- ThreadedSocketAcceptor started ---------");
                acceptorStarted = true;
            } catch (ConfigError configError) {
                configError.printStackTrace();
                log.error("--------- ThreadedSocketAcceptor ran into an error ---------");
            }
        } else {
            logon();
        }
    }

    private void logon (){
        if(threadedSocketAcceptor.getSessions() != null && threadedSocketAcceptor.getSessions().size() > 0) {
            for (SessionID sessionID: threadedSocketAcceptor.getSessions()) {
                Session.lookupSession(sessionID).logon();
            }
            log.info("--------- ThreadedSocketAcceptor logged on to sessions. Size: " + threadedSocketAcceptor.getSessions().size() + " ---------");
        }
    }

    // @Scheduled(every="5s")
    public void serverStatus(){
        log.info("Server Status | Logged on: "+threadedSocketAcceptor.isLoggedOn()+". Current Time: "+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
    }
    
}
