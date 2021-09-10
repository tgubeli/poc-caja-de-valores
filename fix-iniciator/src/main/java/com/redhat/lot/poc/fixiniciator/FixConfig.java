package com.redhat.lot.poc.fixiniciator;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "fix")
public interface FixConfig {
    
	String reconnectInterval();
	String fileStorePath();
	String socketConnectHost();
	String socketConnectPort();
	String targetCompID();
	String beginString();
	String defaultApplVerID();
	String connectionType();
	String heartBtInt();
    String screenLogShowIncoming();
    String screenLogShowOutgoing();

	

}