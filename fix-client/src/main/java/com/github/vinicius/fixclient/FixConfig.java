package com.github.vinicius.fixclient;

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
	

}