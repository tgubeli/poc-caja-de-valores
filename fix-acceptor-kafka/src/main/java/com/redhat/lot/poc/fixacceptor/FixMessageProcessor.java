package com.redhat.lot.poc.fixacceptor;

import javax.enterprise.context.ApplicationScoped;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import quickfix.Message;

@ApplicationScoped
public class FixMessageProcessor {

    @Incoming("marketdata")
    public void process(Message message) throws InterruptedException {

        System.out.println("Message read: "+message.toString());
    }
}
