package com.redhat.lot.poc.fixacceptor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import quickfix.ConfigError;
import quickfix.DoNotSend;
import quickfix.FieldConvertError;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.UnsupportedMessageType;
import quickfix.field.OrdType;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class OrderApplication implements quickfix.Application {
	
    @Inject
    Logger log;

    @ConfigProperty(name = "sendtokafka")
    Boolean sendToKafka;

    @Inject
    KafkaProducer<String, String> producer;

    @Inject
    Metrics metrics;

    private static HashMap<String, FixSessionSender> hashFixSessionSender = new HashMap<>();
    
    public static HashMap<String, FixSessionSender> getHashFixSessionSender() {
		return hashFixSessionSender;
	}


	public static void setHashFixSessionSender(HashMap<String, FixSessionSender> hashFixSessionSender) {
		OrderApplication.hashFixSessionSender = hashFixSessionSender;
	}

    @Override
	public void onCreate(SessionID sessionID) {
        // Session.lookupSession(sessionID).getLog().onEvent("Valid order types: " + validOrderTypes);
    }

    @Override
	public void onLogon(SessionID sessionID) {
        FixSessionSender sender; 
        if (sendToKafka){
            sender = new FixSessionSender(sessionID, this.producer, metrics);
        }else{
            sender = new FixSessionSender(sessionID, metrics);
        }

    	hashFixSessionSender.put(sessionID.toString(), sender);
    	Thread thread = new Thread(sender);
    	thread.start();   	

    	log.info("OrderApplication.onLogon() "+sessionID);
    }

    @Override
	public void onLogout(SessionID sessionID) {
    	//FIXME remove localEventBus consumer
    	log.info("OrderApplication.onLogout() "+sessionID);
    	//TODO Destruir el thread
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
	public void fromAdmin(quickfix.Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        log.info("--------- fromAdmin ---------");
    }

    @Override
	public void fromApp(quickfix.Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        log.info("--------- fromApp ---------");
        
    }

}
