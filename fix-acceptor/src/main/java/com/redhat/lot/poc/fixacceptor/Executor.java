package com.redhat.lot.poc.fixacceptor;

import static quickfix.Acceptor.SETTING_ACCEPTOR_TEMPLATE;
import static quickfix.Acceptor.SETTING_SOCKET_ACCEPT_ADDRESS;
import static quickfix.Acceptor.SETTING_SOCKET_ACCEPT_PORT;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FieldConvertError;
import quickfix.FileStoreFactory;
import quickfix.LogFactory;
import quickfix.MemoryStoreFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.RuntimeError;
import quickfix.ScreenLogFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;
import quickfix.ThreadedSocketAcceptor;
import quickfix.mina.acceptor.DynamicAcceptorSessionProvider;
import quickfix.mina.acceptor.DynamicAcceptorSessionProvider.TemplateMapping;

public class Executor {
    //private final static Logger log = LoggerFactory.getLogger(Executor.class);
   
	private final ThreadedSocketAcceptor acceptor;
    private final Map<InetSocketAddress, List<TemplateMapping>> dynamicSessionMappings = new HashMap<>();


    public Executor(SessionSettings settings) throws Exception {
        
    	OrderApplication application = new OrderApplication(settings);
       
    	//MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
        MessageStoreFactory messageStoreFactory = new MemoryStoreFactory();
        
        // LOG FIX Messages: Booleans (incoming, outgoing, events)
        LogFactory logFactory = new ScreenLogFactory(false, false, true);
        
        MessageFactory messageFactory = new DefaultMessageFactory();

        acceptor = new ThreadedSocketAcceptor(application, messageStoreFactory, settings, logFactory,
                messageFactory);

        configureDynamicSessions(settings, application, messageStoreFactory, logFactory,
                messageFactory);

    }

    private void configureDynamicSessions(SessionSettings settings, OrderApplication application,
            MessageStoreFactory messageStoreFactory, LogFactory logFactory,
            MessageFactory messageFactory) throws ConfigError, FieldConvertError {
        //
        // If a session template is detected in the settings, then
        // set up a dynamic session provider.
        //

        Iterator<SessionID> sectionIterator = settings.sectionIterator();
        while (sectionIterator.hasNext()) {
            SessionID sessionID = sectionIterator.next();
            if (isSessionTemplate(settings, sessionID)) {
                InetSocketAddress address = getAcceptorSocketAddress(settings, sessionID);
                getMappings(address).add(new TemplateMapping(sessionID, sessionID));
            }
        }

        for (Map.Entry<InetSocketAddress, List<TemplateMapping>> entry : dynamicSessionMappings
                .entrySet()) {
            acceptor.setSessionProvider(entry.getKey(), new DynamicAcceptorSessionProvider(
                    settings, entry.getValue(), application, messageStoreFactory, logFactory,
                    messageFactory));
        }
    }

    private List<TemplateMapping> getMappings(InetSocketAddress address) {
        return dynamicSessionMappings.computeIfAbsent(address, k -> new ArrayList<>());
    }

    private InetSocketAddress getAcceptorSocketAddress(SessionSettings settings, SessionID sessionID)
            throws ConfigError, FieldConvertError {
        String acceptorHost = "0.0.0.0";
        if (settings.isSetting(sessionID, SETTING_SOCKET_ACCEPT_ADDRESS)) {
            acceptorHost = settings.getString(sessionID, SETTING_SOCKET_ACCEPT_ADDRESS);
        }
        int acceptorPort = (int) settings.getLong(sessionID, SETTING_SOCKET_ACCEPT_PORT);

        return new InetSocketAddress(acceptorHost, acceptorPort);
    }

    private boolean isSessionTemplate(SessionSettings settings, SessionID sessionID)
            throws ConfigError, FieldConvertError {
        return settings.isSetting(sessionID, SETTING_ACCEPTOR_TEMPLATE)
                && settings.getBool(sessionID, SETTING_ACCEPTOR_TEMPLATE);
    }

    public void start() throws RuntimeError, ConfigError {
    	System.out.println("Executor.start()");
        acceptor.start();
    }

    public void stop() {
        acceptor.stop();
    }

//    public static void main(String[] args) throws Exception {
//        try {
//            InputStream inputStream = getSettingsInputStream(args);
//            SessionSettings settings = new SessionSettings(inputStream);
//            inputStream.close();
//
//            Executor executor = new Executor(settings);
//            executor.start();
//
//            System.out.println("press <enter> to quit");
//            System.in.read();
//
//            executor.stop();
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }
//    }

    private static InputStream getSettingsInputStream(String[] args) throws FileNotFoundException {
        InputStream inputStream = null;
        // if (args.length == 0) {
        //     inputStream = Executor.class.getResourceAsStream("executor.cfg");
        // } else if (args.length == 1) {
        //    inputStream = new FileInputStream(new File("./executor.cfg"));
        // }
        if (inputStream == null) {
            System.out.println("using local file at: " + Executor.class.getCanonicalName() + " [configFile].");
            System.exit(1);
        }
        return inputStream;
    }
}
