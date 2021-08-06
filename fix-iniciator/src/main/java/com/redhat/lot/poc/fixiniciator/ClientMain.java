package com.redhat.lot.poc.fixiniciator;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.eclipse.microprofile.config.ConfigProvider;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.Dictionary;
import quickfix.FileStoreFactory;
import quickfix.Initiator;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.ScreenLogFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;
import quickfix.field.BeginString;
import quickfix.field.SenderCompID;
import quickfix.field.TargetCompID;

@QuarkusMain
public class ClientMain implements QuarkusApplication{
	
	private static Initiator initiator = null;
	
	private Logger log = Logger.getLogger(ClientMain.class.getName());
	
	@Inject
	FixConfig fixConfig;
	
	@Override
    public int run(String... args) {

		//public static void main(String[] args) throws Exception {
        System.out.println("Running main method");
        //try (InputStream inputStream = getSettingsInputStream(args)){
            
    	//SessionSettings settings = new SessionSettings(inputStream);
    
    	// Values like SenderCompID, SocketConnectHost and others are read from application.properties or Java/K8s Environment Variables
    	
    	try {
    		
    		SessionSettings settings = newSessionSettings();
        	
        	
            boolean logHeartbeats = Boolean.valueOf(System.getProperty("logHeartbeats", "false"));
            

            MyApplication application = new MyApplication();
            MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
            LogFactory logFactory = new ScreenLogFactory(true, true, true, logHeartbeats);
            MessageFactory messageFactory = new DefaultMessageFactory();

            // QuickFix :: Socket Initiator...
            initiator = new SocketInitiator(application, messageStoreFactory, settings, logFactory, messageFactory);
            
            // QuickFix :: Start Socket Initiator...
            initiator.start();
            System.out.println(">>> Sessions Size: "+initiator.getSessions().size());
            for (SessionID sessionId : initiator.getSessions()) {
                Session.lookupSession(sessionId).logon();
            }
            System.out.println(">>> Logged On?? "+initiator.isLoggedOn());
            
            //Quarkus.run(args); 
            Quarkus.waitForExit();
            logout();
            
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    //}
        return 0;
        
    }
    
    public static void logout() {
        for (SessionID sessionId : initiator.getSessions()) {
            Session.lookupSession(sessionId).logout("user requested");
        }
    } 
    
    private SessionSettings newSessionSettings() throws ConfigError, UnknownHostException{
    	
    	SessionSettings settings = new SessionSettings();
    	
    	String host = InetAddress.getLocalHost().getHostName();
    	
    	SessionID id1 = new SessionID(new BeginString("FIXT.1.1"), 
    			new SenderCompID(host), 
    			new TargetCompID(fixConfig.targetCompID()), "Session1");
        Dictionary d = new Dictionary();
        
        System.out.println(">>> FIX Properties Values: "
        		+"\n\t ConnectionType: '"+ fixConfig.connectionType()+"'"
        		+"\n\t SocketConnectPort: '"+ fixConfig.socketConnectPort()+"'"
        		+"\n\t SocketConnectHost: '"+ fixConfig.socketConnectHost()+"'"
        		+"\n\t FileStorePath: '"+ fixConfig.fileStorePath()+"'"
        		+"\n\t HeartBtInt: '"+ fixConfig.heartBtInt()+"'"
        		+"\n\t ReconnectInterval: '"+ fixConfig.reconnectInterval()+"'"
        		+"\n\t DefaultApplVerID: '"+ fixConfig.defaultApplVerID()+"'"
        );
        
        d.setString("ConnectionType", fixConfig.connectionType());
        d.setString("SocketConnectPort", fixConfig.socketConnectPort());
        d.setString("SocketConnectHost", fixConfig.socketConnectHost());
        d.setString("StartTime", "00:00:00");
        d.setString("EndTime", "00:00:00");
        d.setString("FileStorePath", fixConfig.fileStorePath());
        d.setBool("UseDataDictionary", true);
        d.setString("DataDictionary", "FIX42.xml");
        d.setString("HeartBtInt", fixConfig.heartBtInt());
        d.setString("ReconnectInterval", fixConfig.reconnectInterval());
        d.setString("ResetOnLogout", "Y");
        d.setString("ResetOnLogon", "Y");
        d.setString("ResetOnDisconnect", "Y");
        d.setString("DefaultApplVerID", fixConfig.defaultApplVerID());
        
        settings.set(id1,d);
        
        return settings;
    	
    }
    
//    private static SessionSettings setCustomValues(SessionSettings ss) throws UnknownHostException{
//    	
//    	String host = InetAddress.getLocalHost().getHostName();
//    	
//    	for( final Iterator<SessionID> i = ss.sectionIterator(); i.hasNext(); ) {
//    	    final SessionID id = i.next();
//    	    //if( id.getSenderCompID().startsWith("quote") ) {
//    	        ss.setString( id, "SocketConnectHost", socketConnectHost );
//    	        ss.setString( id, "ReconnectInterval", socketConnectHost );
//    	        ss.setString( id, "FileStorePath", socketConnectHost );
//    	        ss.setString( id, "SocketConnectPort", socketConnectHost );
//    	        ss.setString( id, "LogoutTimeout", socketConnectHost );
//    	        ss.setString( id, "SenderCompID", host);
//    	        ss.setString( id, "TargetCompID", targetCompID);
//    	    //}
//    	}
//    	
//    	return ss;
//    	
//    }
    
//    private static SessionSettings getSessionSettings() throws Exception{
//    	
//    	SessionSettings settings = new SessionSettings();
//    	
//    	Dictionary defaultDic = new Dictionary();
//
//    	try{
//    		
//    	
//	        defaultDic.setString("ConnectionType","initiator");
//	        defaultDic.setString("ReconnectInterval", reconnectInterval); // Loaded from: properties file or K8s Environment Variable or Configmap
//	        defaultDic.setString("FileStorePath", fileStorePath); // Loaded from: properties file or K8s Environment Variable or Configmap
//	        defaultDic.setString("StartTime", "00:00:00");
//	        defaultDic.setString("EndTime", "00:00:00");
//	        defaultDic.setString("SocketConnectHost", socketConnectHost); // Loaded from: properties file or K8s Environment Variable or Configmap
//	        defaultDic.setString("SocketConnectPort", socketConnectPort); // Loaded from: properties file or K8s Environment Variable or Configmap
//	        defaultDic.setString("LogoutTimeout", logoutTimeout); // Loaded from: properties file or K8s Environment Variable or Configmap
//	        defaultDic.setBool("ResetOnLogon", true);
//	        defaultDic.setBool("ResetOnDisconnect", true);
//	        
//	        defaultDic.setBool("UseDataDictionary", true);
//	        defaultDic.setString("DataDictionary", "FIX44.xml");
//	        defaultDic.setString("FileLogPath", "log");
//	
//	        settings.set(defaultDic);
//	
//	
//	        Dictionary dic = new Dictionary();
//	        
//	        String host = InetAddress.getLocalHost().getHostName();
//	
//	        dic.setString("BeginString", "FIX.4.4");
//	        dic.setString("SenderCompID", host);     // host is a string with hostname value (pod name)
//	        //dic.setString("TargetCompID", "ServerAcceptor");
//	        dic.setString("TargetCompID", "STUN");
//	        dic.setString("HeartBtInt", "30");
//	
//	
//	        SessionID sID = new SessionID("FIX.4.4", host, "ServerAcceptor");
//	        settings.set(sID, dic);
//	        
//    	}catch(ConfigError confError) {
//    		//log.log(Level.SEVERE, "There was an error loading default Session Settings: \n "+confError.getMessage());
//    		confError.printStackTrace();
//    		throw new Exception("There was an error loading default Session Settings: \n "+confError.getMessage());
//    	}catch(UnknownHostException uhe) {
//    		//log.log(Level.SEVERE, "There was an error loading the local host name: \n "+uhe.getMessage());
//    		throw new Exception("There was an error loading the local host name: \n "+uhe.getMessage());
//    	}
//        
//        return settings;
//    }
    
//    private static InputStream getSettingsInputStream(String[] args) throws FileNotFoundException {
//    	InputStream inputStream = null;
//		System.out.println(Arrays.toString(args));
//		if(args.length == 1) {
//			System.out.println("Arquito de conf: "+args[0]);
//			inputStream = new FileInputStream(new File(args[0]));
//		}else {
//			inputStream = ClientMain.class.getResourceAsStream("settings.cfg");	
//		}
//        if (inputStream == null) {
//            System.out.println("usage: " + ClientMain.class.getName() + " [configFile].");
//            System.exit(1);
//        }else {
//        	System.out.println("Arquivo de conf do FIX encontrado com sucesso!");
//        }
//        return inputStream;
//    }

}