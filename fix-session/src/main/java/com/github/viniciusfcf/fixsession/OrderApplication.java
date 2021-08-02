package com.github.viniciusfcf.fixsession;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import javax.enterprise.inject.spi.CDI;

import org.jboss.logging.Logger;

import io.vertx.mutiny.core.eventbus.EventBus;
import quickfix.ConfigError;
import quickfix.DoNotSend;
import quickfix.FieldConvertError;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.UnsupportedMessageType;
import quickfix.field.OrdType;

public class OrderApplication implements quickfix.Application {
	
	public static final Logger LOG = Logger.getLogger(OrderApplication.class);

    private static final String VALID_ORDER_TYPES_KEY = "ValidOrderTypes";

    private final HashSet<String> validOrderTypes = new HashSet<>();
    
    FixSessionSender sender;

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
    	
      
      LocalDateTime now = LocalDateTime.now();
      //LOG.infof("session %s time: %s ms", sessionID, (ChronoUnit.MILLIS.between(0, now)));
      LOG.infof("logon");
    	sender = new FixSessionSender(sessionID);
    	Thread thread = new Thread(sender);
    	thread.start();
    	System.out.println("SessionSender creado");
    	
//    	EventBus eventBus = CDI.current().select(EventBus.class).get();
//    	Consumer<io.vertx.mutiny.core.eventbus.Message<String>> consumer = new Consumer<io.vertx.mutiny.core.eventbus.Message<String>>() {
//            @Override
//			public void accept(io.vertx.mutiny.core.eventbus.Message<String> eventBusMsg) {
//
//                LocalDateTime inicio = LocalDateTime.parse(eventBusMsg.headers().get("publishTimestamp"));
//                LocalDateTime now = LocalDateTime.now();
//                LOG.infof("session %s time: %s ms", sessionID, (ChronoUnit.MILLIS.between(inicio, now)));
//                try {
//                    String msg = eventBusMsg.body();
//                    // TODO identifyType should be necessary
//            		// MsgType identifyType = Message.identifyType(msg);
//            		// System.out.println(identifyType);
//            		Message message = new Message();
//            		message.fromString(msg, null, false);
//					Session.sendToTarget(message, sessionID);
//				} catch (Exception e) {
//                    LOG.debug("Error", e);
//				}
//            }
//        };
//        eventBus.localConsumer("quotas", consumer);
    	System.out.println("OrderApplication.onLogon() "+sessionID);
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
