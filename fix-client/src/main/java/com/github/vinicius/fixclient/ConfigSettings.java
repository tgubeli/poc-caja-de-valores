package com.github.vinicius.fixclient;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.ConfigProvider;

import quickfix.ConfigError;
import quickfix.Dictionary;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.field.BeginString;
import quickfix.field.SenderCompID;
import quickfix.field.TargetCompID;

@ApplicationScoped
public class ConfigSettings {
	
	private SessionSettings sessionSettings;



	public ConfigSettings() {
		super();
	}
	
	public void setSessionSettings(SessionSettings sessionSettings) {
		this.sessionSettings = sessionSettings;
	}
	
	

	public SessionSettings getSessionSettings() throws ConfigError, UnknownHostException{
    	
    	SessionSettings settings = new SessionSettings();
    	
    	String host = InetAddress.getLocalHost().getHostName();
    	
    	String reconnectInterval = ConfigProvider.getConfig().getValue("fix.reconnectInterval",String.class);
    	String fileStorePath = ConfigProvider.getConfig().getValue("fix.fileStorePath",String.class);
    	String socketConnectHost = ConfigProvider.getConfig().getValue("fix.socketConnectHost",String.class);
    	String socketConnectPort = ConfigProvider.getConfig().getValue("fix.socketConnectPort",String.class);
    	String logoutTimeout = ConfigProvider.getConfig().getValue("fix.logoutTimeout",String.class);
    	String targetCompID = ConfigProvider.getConfig().getValue("fix.targetCompID",String.class);
    	
    	SessionID id1 = new SessionID(new BeginString("FIXT.1.1"), 
    			new SenderCompID(host), 
    			new TargetCompID("STUN"), "Session1");
        Dictionary d = new Dictionary();
        
        d.setString("ConnectionType", "initiator");
        d.setString("SocketConnectPort", "8199");
        d.setString("SocketConnectHost", "localhost");
        d.setString("StartTime", "00:00:00");
        d.setString("EndTime", "00:00:00");
        d.setString("FileStorePath", "/tmp/banzai/data");
        d.setBool("UseDataDictionary", true);
        d.setString("DataDictionary", "FIX42.xml");
        d.setString("HeartBtInt", "30");
        d.setString("ReconnectInterval", "5");
        d.setString("ResetOnLogout", "Y");
        d.setString("ResetOnLogon", "Y");
        d.setString("ResetOnDisconnect", "Y");
        d.setString("DefaultApplVerID", "FIX.5.0SP2");
        
        settings.set(id1,d);
        
        return settings;
    	
    }
	
}
