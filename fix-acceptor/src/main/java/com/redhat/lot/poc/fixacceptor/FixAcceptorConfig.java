package com.redhat.lot.poc.fixacceptor;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import static quickfix.Acceptor.SETTING_ACCEPTOR_TEMPLATE;

import quickfix.mina.acceptor.DynamicAcceptorSessionProvider;
import quickfix.mina.acceptor.DynamicAcceptorSessionProvider.TemplateMapping;

import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FieldConvertError;
import quickfix.LogFactory;
import quickfix.MemoryStoreFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.ScreenLogFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.ThreadedSocketAcceptor;

@ApplicationScoped
public class FixAcceptorConfig {

	@Inject
	OrderApplication application;

	@ConfigProperty(name = "quickfixj.server.config")
    String serverConfig;

	private final Map<InetSocketAddress, List<TemplateMapping>> dynamicSessionMappings = new HashMap<>();

	@Produces
	public ThreadedSocketAcceptor threadedSocketAcceptor() throws FieldConvertError {
		ThreadedSocketAcceptor acceptor = null;
		try {
			SessionSettings settings = new SessionSettings(FixAcceptorConfig.class.getResourceAsStream(serverConfig));
			// MessageStoreFactory storeFactory = new FileStoreFactory(settings);
			MessageStoreFactory storeFactory = new MemoryStoreFactory();
			LogFactory logFactory = new ScreenLogFactory(false, false, true);
			MessageFactory messageFactory = new DefaultMessageFactory();
			ThreadedSocketAcceptor threadedSocketacceptor = new ThreadedSocketAcceptor(application, storeFactory,
					settings, logFactory, messageFactory);

			Iterator<SessionID> sectionIterator = settings.sectionIterator();
			while (sectionIterator.hasNext()) {
				SessionID sessionID = sectionIterator.next();
				if (isSessionTemplate(settings, sessionID)) {
					InetSocketAddress address = getAcceptorSocketAddress(settings, sessionID);
					getMappings(address).add(new TemplateMapping(sessionID, sessionID));
				}
			}
	
			for (Map.Entry<InetSocketAddress, List<TemplateMapping>> entry : dynamicSessionMappings.entrySet()) {
				threadedSocketacceptor.setSessionProvider(entry.getKey(), new DynamicAcceptorSessionProvider(
						settings, entry.getValue(), application, storeFactory, logFactory,
						messageFactory));
			}
			
			acceptor = threadedSocketacceptor;
		} catch (ConfigError configError) {
			configError.printStackTrace();
		}
		return acceptor;
	}

	private InetSocketAddress getAcceptorSocketAddress(SessionSettings settings, SessionID sessionID)
			throws ConfigError, FieldConvertError {
		String acceptorHost = "0.0.0.0";
		if (settings.isSetting(sessionID, quickfix.Acceptor.SETTING_SOCKET_ACCEPT_ADDRESS)) {
			acceptorHost = settings.getString(sessionID, quickfix.Acceptor.SETTING_SOCKET_ACCEPT_ADDRESS);
		}
		int acceptorPort = (int) settings.getLong(sessionID, quickfix.Acceptor.SETTING_SOCKET_ACCEPT_PORT);

		return new InetSocketAddress(acceptorHost, acceptorPort);
	}

	private List<TemplateMapping> getMappings(InetSocketAddress address) {
        return dynamicSessionMappings.computeIfAbsent(address, k -> new ArrayList<>());
    }

	private boolean isSessionTemplate(SessionSettings settings, SessionID sessionID)
            throws ConfigError, FieldConvertError {
        return settings.isSetting(sessionID, SETTING_ACCEPTOR_TEMPLATE)
                && settings.getBool(sessionID, SETTING_ACCEPTOR_TEMPLATE);
    }
    
}
