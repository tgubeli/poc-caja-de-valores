package com.redhat.lot.poc.fixacceptor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.logging.Logger;

import quickfix.ConfigError;
import quickfix.DoNotSend;
import quickfix.FieldConvertError;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.InvalidMessage;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.SessionSettings;
import quickfix.UnsupportedMessageType;
import quickfix.field.OrdType;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import org.apache.kafka.clients.consumer.ConsumerRecords;

import javax.enterprise.event.Observes;

import java.time.Duration;
import java.util.Collections;

@ApplicationScoped
public class ServerApplicationAdapter implements quickfix.Application {
	
    @Inject
	Logger log;

    @Inject
    KafkaConsumer<String, String> consumer;

    @ConfigProperty(name = "kafka.topic")
	String kafkaTopic;

    boolean done = false;
    volatile String last;

    @Override
	public void onCreate(SessionID sessionID) {
        log.info("--------- onCreate ---------");
        // Session.lookupSession(sessionID).getLog().onEvent("Valid order types: " + validOrderTypes);
    }

    @Override
	public void onLogon(SessionID sessionID) {
        log.info("--------- onLogon ---------");
        log.info("--------- Starting kafka consumer Thread ---------");
        consumer.subscribe(Collections.singleton(kafkaTopic));
            
        while (! done) {
            final ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofSeconds(1));
            consumerRecords.forEach(record -> {
                System.out.printf("Polled Record:(%s, %s, %d, %d)\n",
                            record.key(), record.value(),
                            record.partition(), record.offset());
                last = record.key() + "-" + record.value();

                try {
                    Message fixMessage = new Message();
                    fixMessage.fromString(record.value(), null, false);
                    Session.sendToTarget(fixMessage, sessionID);
                } catch (InvalidMessage e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (SessionNotFound e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
            });
        }
        
        consumer.close();
    }

    @Override
	public void onLogout(SessionID sessionID) {
        log.info("--------- onLogout ---------");
        done = false;
    }

    @Override
	public void toAdmin(quickfix.Message message, SessionID sessionID) {
        log.info("--------- toAdmin ---------");
    }

    @Override
	public void toApp(quickfix.Message message, SessionID sessionID) throws DoNotSend {
        log.info("--------- toApp ---------");
    }

    @Override
	public void fromAdmin(quickfix.Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat,
            IncorrectTagValue, RejectLogon {
        log.info("--------- fromAdmin ---------");
    }

    @Override
	public void fromApp(quickfix.Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat,
            IncorrectTagValue, UnsupportedMessageType {
        log.info("--------- fromApp ---------");
        
    }

}
