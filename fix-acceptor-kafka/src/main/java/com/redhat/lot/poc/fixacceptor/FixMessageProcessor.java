package com.redhat.lot.poc.fixacceptor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import quickfix.ConfigError;
import quickfix.InvalidMessage;
import quickfix.Message;
import quickfix.MessageUtils;
import quickfix.RuntimeError;
import quickfix.Session;
import quickfix.SessionSettings;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

@ApplicationScoped
public class FixMessageProcessor {

    private Logger LOGGER = Logger.getLogger(FixMessageProcessor.class.getName());
    private Executor executor;

    @Produces
    public Executor executor() throws Exception {
        InputStream inputStream = getSettingsInputStream();
        SessionSettings settings = new SessionSettings(inputStream);
       
        Executor executor = new Executor(settings);
        return executor;
    }

    private InputStream getSettingsInputStream(){
		InputStream inputStream = Executor.class.getResourceAsStream("executor.cfg");
		if (inputStream == null) {
			System.out.println("usage: " + Executor.class.getName() + " [configFile].");
			System.exit(1);
		} else {
			System.out.println("Arquivo de conf de FIX encontrado!");
		}
		return inputStream;
	}

    @PostConstruct 
    void init() throws Exception {
        InputStream inputStream = getSettingsInputStream();
        SessionSettings settings = new SessionSettings(inputStream);
       
        executor = new Executor(settings);
        executor.start();
    }

    @Incoming("marketdata")
    public void process(String msg) throws InterruptedException, InvalidMessage {
        LOGGER.info("Fix Message received: "+msg.toString());
        // Message message = MessageUtils.parse(this.executor.getApplication().getFixSession(), msg);
        // if (message != null){
        //     System.out.println("Message succesfully received");
        // }
    }

    @PreDestroy
    public void destroy(){
        executor.stop();
    }
}
