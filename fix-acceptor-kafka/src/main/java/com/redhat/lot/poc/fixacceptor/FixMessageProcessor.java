package com.redhat.lot.poc.fixacceptor;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.util.logging.Logger;

@ApplicationScoped
public class FixMessageProcessor {

    private Logger LOGGER = Logger.getLogger(FixMessageProcessor.class.getName());


    @Incoming("marketdata")
    public void process(String message) throws InterruptedException {

        LOGGER.info("Fix Message received: "+message.toString());

    }
}
