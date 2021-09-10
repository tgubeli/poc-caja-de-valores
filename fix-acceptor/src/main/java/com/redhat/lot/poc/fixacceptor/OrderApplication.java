package com.redhat.lot.poc.fixacceptor;

import java.util.HashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.RejectLogon;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;

import org.apache.kafka.clients.producer.KafkaProducer;

import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class OrderApplication implements quickfix.Application {
	
    @Inject
    Logger log;

    @Inject
    KafkaProducer<String, String> producer;

    @Inject
    Metrics metrics;

    @Scheduled(every="5s")     
    void showMetrics() {
        metrics.logMetrics();
    }

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
        FixSessionSender sender = new FixSessionSender(sessionID, metrics);

    	hashFixSessionSender.put(sessionID.toString(), sender);
    	Thread thread = new Thread(sender);
    	thread.start();   	

    	log.info("OrderApplication.onLogon() "+sessionID);
    }

    @Override
	public void onLogout(SessionID sessionID) {
    	log.info("OrderApplication.onLogout() "+sessionID);
        hashFixSessionSender.get(sessionID.toString()).stop();
    	hashFixSessionSender.remove(sessionID.toString());
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
