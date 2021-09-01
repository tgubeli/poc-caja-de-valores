package com.redhat.lot.poc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class OrderApplication implements quickfix.Application {
	
	public static final Logger LOG = LoggerFactory.getLogger(OrderApplication.class);

    private static final String VALID_ORDER_TYPES_KEY = "ValidOrderTypes";

    private final HashSet<String> validOrderTypes = new HashSet<>();
    
    private static HashMap<String, FixSessionSender> hashFixSessionSender = new HashMap<>();
    
    //FixSessionSender sender;

    public static HashMap<String, FixSessionSender> getHashFixSessionSender() {
		return hashFixSessionSender;
	}


	public static void setHashFixSessionSender(HashMap<String, FixSessionSender> hashFixSessionSender) {
		OrderApplication.hashFixSessionSender = hashFixSessionSender;
	}


	public OrderApplication(SessionSettings settings) throws ConfigError, FieldConvertError {
        initializeValidOrderTypes(settings);

    }


    private void initializeValidOrderTypes(SessionSettings settings) throws ConfigError, FieldConvertError {
        if (settings.isSetting(VALID_ORDER_TYPES_KEY)) {
            List<String> orderTypes = Arrays
                    .asList(settings.getString(VALID_ORDER_TYPES_KEY).trim().split("\\s*,\\s*"));
            validOrderTypes.addAll(orderTypes);
        } else {
            validOrderTypes.add(OrdType.LIMIT + "");
        }
    }

    @Override
	public void onCreate(SessionID sessionID) {
        Session.lookupSession(sessionID).getLog().onEvent("Valid order types: " + validOrderTypes);
    }

    @Override
	public void onLogon(SessionID sessionID) {
    	
    	try {
	    	
    		LOG.info("logon");
	    	
//	    	int cores = Runtime.getRuntime().availableProcessors();
//	        System.out.println("Number of cores: " + cores); // cores
//	
//	        ExecutorService threadPool = Executors.newFixedThreadPool(cores);
//	        
//	        FixSessionSenderSimple sender = new FixSessionSenderSimple(sessionID);
//	        Future<Boolean> future = CompletableFuture.supplyAsync(() -> sender.run(),threadPool);
//	
//	        Boolean result = future.get();
	        
	    	
	      	//LOG.info("logon");
	      	FixSessionSender sender = new FixSessionSender(sessionID);
	      	hashFixSessionSender.put(sessionID.toString(), sender);
	    	Thread thread = new Thread(null,sender,"FixSessionSender-for-"+sessionID.toString());
	    	//thread.setPriority(9);
	    	thread.start();   	
	
	    	System.out.println("OrderApplication.onLogon() "+sessionID);
	    	
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    }

    @Override
	public void onLogout(SessionID sessionID) {
    	//FIXME remove localEventBus consumer
    	System.out.println("OrderApplication.onLogout() "+sessionID);
    	//TODO Destruir el thread
    }

    @Override
	public void toAdmin(quickfix.Message message, SessionID sessionID) {
    }

    @Override
	public void toApp(quickfix.Message message, SessionID sessionID) throws DoNotSend {
    }

    @Override
	public void fromAdmin(quickfix.Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat,
            IncorrectTagValue, RejectLogon {
    }

    @Override
	public void fromApp(quickfix.Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat,
            IncorrectTagValue, UnsupportedMessageType {
        // System.out.println(message.toRawString());
    }

}
