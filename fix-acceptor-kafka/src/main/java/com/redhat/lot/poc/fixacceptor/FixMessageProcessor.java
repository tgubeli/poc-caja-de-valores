package com.redhat.lot.poc.fixacceptor;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import quickfix.DoubleField;
import quickfix.FieldNotFound;
import quickfix.InvalidMessage;
import quickfix.Message;

import java.util.logging.Logger;

@ApplicationScoped
public class FixMessageProcessor {

    private Logger LOGGER = Logger.getLogger(FixMessageProcessor.class.getName());

    @Incoming("marketdata")
    public void process(String msg) throws InterruptedException, FieldNotFound, InvalidMessage {
        Message fixMessage = new Message();
        fixMessage.fromString(msg, null, false);
        LOGGER.info("Fix Message received field (60): "+fixMessage.getField(new DoubleField(60)).toString());
    }

}
