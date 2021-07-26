package com.github.viniciusfcf.fixsession;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.spi.CDI;

import org.jboss.logging.Logger;

import io.vertx.core.DeploymentOptions;
import io.vertx.mutiny.core.Vertx;
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
	
	public static final Logger LOG = Logger.getLogger(OrderApplication.class);

    private static final String VALID_ORDER_TYPES_KEY = "ValidOrderTypes";

    private final HashSet<String> validOrderTypes = new HashSet<>();
    
    public static final Map<SessionID, String> mapSessionToDeploymentID = Collections.synchronizedMap(new HashMap<>());

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
    	Vertx vertx = CDI.current().select(Vertx.class).get();
    	DeploymentOptions options = new DeploymentOptions();
    	options.setWorker(true);
		String deploymentID = vertx.deployVerticle(new OrderVerticle(sessionID), options).await().indefinitely();
    	mapSessionToDeploymentID.put(sessionID, deploymentID);
    	System.out.println("OrderApplication.onLogon() "+sessionID+ " "+deploymentID);
    }

    @Override
	public void onLogout(SessionID sessionID) {
    	String deploymentID = mapSessionToDeploymentID.remove(sessionID);
    	Vertx vertx = CDI.current().select(Vertx.class).get();
    	vertx.undeploy(deploymentID);
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
//        System.out.println(message.toRawString());
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


}
