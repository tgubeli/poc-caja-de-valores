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
import quickfix.FixVersions;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.MessageUtils;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.UnsupportedMessageType;
import quickfix.field.ApplVerID;
import quickfix.field.OrdType;

public class OrderApplication implements quickfix.Application {
	
	public static final Logger LOG = Logger.getLogger(OrderApplication.class);

    private static final String VALID_ORDER_TYPES_KEY = "ValidOrderTypes";

    private final HashSet<String> validOrderTypes = new HashSet<>();

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
    	EventBus eventBus = CDI.current().select(EventBus.class).get();
    	Consumer<io.vertx.mutiny.core.eventbus.Message<Integer>> consumer = new Consumer<io.vertx.mutiny.core.eventbus.Message<Integer>>() {
            @Override
			public void accept(io.vertx.mutiny.core.eventbus.Message<Integer> t) {

                LocalDateTime inicio = LocalDateTime.parse(t.headers().get("publishTimestamp"));
                LocalDateTime now = LocalDateTime.now();
                LOG.infof("session %s time: %s ms", sessionID, (ChronoUnit.MILLIS.between(inicio, now)));
            }
        };
        //add 2 consumers
        eventBus.localConsumer("quotas", consumer);
    	//FIXME create local EventBus consumer
    	System.out.println("OrderApplication.onLogon() "+sessionID);
    }

    @Override
	public void onLogout(SessionID sessionID) {
    	//FIXME remove localEventBus consumer
    	System.out.println("OrderApplication.onLogout() "+sessionID);
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
        System.out.println(message.toRawString());
    }

//    private void sendMessage(SessionID sessionID, Message message) {
//        try {
//            Session session = Session.lookupSession(sessionID);
//            if (session == null) {
//                throw new SessionNotFound(sessionID.toString());
//            }
//
//            DataDictionaryProvider dataDictionaryProvider = session.getDataDictionaryProvider();
//            if (dataDictionaryProvider != null) {
//                try {
//                    dataDictionaryProvider.getApplicationDataDictionary(
//                            getApplVerID(session, message)).validate(message, true);
//                } catch (Exception e) {
//                    LogUtil.logThrowable(sessionID, "Outgoing message failed validation: "
//                            + e.getMessage(), e);
//                    return;
//                }
//            }
//
//            session.send(message);
//        } catch (SessionNotFound e) {
//            log.error(e.getMessage(), e);
//        }
//    }

    private ApplVerID getApplVerID(Session session, Message message) {
        String beginString = session.getSessionID().getBeginString();
        if (FixVersions.BEGINSTRING_FIXT11.equals(beginString)) {
            return new ApplVerID(ApplVerID.FIX50);
        } else {
            return MessageUtils.toApplVerID(beginString);
        }
    }

}
