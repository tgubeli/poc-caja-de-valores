package com.redhat.lot.poc.fixacceptor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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

public class OrderApplication implements quickfix.Application {
	
	public static final Logger LOG = Logger.getLogger(OrderApplication.class);

    private static final String VALID_ORDER_TYPES_KEY = "ValidOrderTypes";
    private SessionID sessionID;
    private Session fixSession = null;

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
        LOG.infof("OrderApplication.onLogon()");
        LOG.infof("SessionID: " +sessionID);
        this.sessionID = sessionID;
        this.fixSession = Session.lookupSession(sessionID);
    }

    @Override
	public void onLogout(SessionID sessionID) {
    	//FIXME remove localEventBus consumer
    	LOG.infof("OrderApplication.onLogout() "+sessionID);
    	//TODO Destruir el thread
    }

    @Override
	public void toAdmin(quickfix.Message message, SessionID sessionID) {}

    @Override
	public void toApp(quickfix.Message message, SessionID sessionID) throws DoNotSend {}

    @Override
	public void fromAdmin(quickfix.Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat,IncorrectTagValue, RejectLogon {}

    @Override
	public void fromApp(quickfix.Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {}

    public SessionID getSessionID(){
        return this.sessionID;
    }

    public Session getFixSession(){
        return this.fixSession;
    }

}
