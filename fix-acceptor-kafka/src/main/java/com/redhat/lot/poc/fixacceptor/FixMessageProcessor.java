package com.redhat.lot.poc.fixacceptor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import quickfix.ConfigError;
import quickfix.DoubleField;
import quickfix.FieldNotFound;
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
    
    @Incoming("marketdata")
    public void process(String msg) throws InterruptedException, FieldNotFound, InvalidMessage {
        // LOGGER.info("Fix Message received: "+msg.toString());
        Message fixMessage = new Message();
        fixMessage.fromString(msg, null, false);
        LOGGER.info(">>>>>>> "+fixMessage.getField(new DoubleField(60)).toString());
    }

}
