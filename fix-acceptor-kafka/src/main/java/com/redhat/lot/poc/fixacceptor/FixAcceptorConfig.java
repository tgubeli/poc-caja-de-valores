package com.redhat.lot.poc.fixacceptor;

import java.net.InetSocketAddress;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FieldConvertError;
import quickfix.FileLogFactory;
import quickfix.FileStoreFactory;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.ThreadedSocketAcceptor;
import quickfix.mina.acceptor.DynamicAcceptorSessionProvider;

@ApplicationScoped
public class FixAcceptorConfig {

	@Inject
	ServerApplicationAdapter application;

	@ConfigProperty(name = "quickfixj.server.config")
    String serverConfig;

	@Produces
	public ThreadedSocketAcceptor threadedSocketAcceptor() {
		ThreadedSocketAcceptor acceptor = null;
		try {
			SessionSettings settings = new SessionSettings(FixAcceptorConfig.class.getResourceAsStream(serverConfig));
			MessageStoreFactory storeFactory = new FileStoreFactory(settings);
			LogFactory logFactory = new FileLogFactory(settings);
			MessageFactory messageFactory = new DefaultMessageFactory();
			final ThreadedSocketAcceptor threadedSocketacceptor = new ThreadedSocketAcceptor(application, storeFactory,
					settings, logFactory, messageFactory);

			settings.sectionIterator().forEachRemaining(s -> {
				InetSocketAddress address;
				try {
					address = getAcceptorSocketAddress(settings, s);
					DynamicAcceptorSessionProvider provider = new DynamicAcceptorSessionProvider(settings, s, application,
							storeFactory, logFactory, messageFactory);
					threadedSocketacceptor.setSessionProvider(address, provider);
				} catch (ConfigError | FieldConvertError e) {
					e.printStackTrace();
				}

			});

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

		InetSocketAddress address = new InetSocketAddress(acceptorHost, acceptorPort);
		return address;
	}
    
}
