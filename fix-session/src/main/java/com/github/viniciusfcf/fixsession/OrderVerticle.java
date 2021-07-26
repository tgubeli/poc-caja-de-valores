package com.github.viniciusfcf.fixsession;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

import org.jboss.logging.Logger;

import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import quickfix.SessionID;

public class OrderVerticle extends AbstractVerticle {

	public static final Logger LOG = Logger.getLogger(OrderVerticle.class);

	private SessionID sessionID;
	
	public OrderVerticle(SessionID sessionID) {
		this.sessionID = sessionID;
	}

	@Override
	public void start(Promise<Void> startPromise) throws Exception {

		Consumer<io.vertx.mutiny.core.eventbus.Message<String>> consumer = new Consumer<io.vertx.mutiny.core.eventbus.Message<String>>() {
            @Override
			public void accept(io.vertx.mutiny.core.eventbus.Message<String> t) {
            	LocalDateTime now = LocalDateTime.now();
                LocalDateTime inicio = LocalDateTime.parse(t.headers().get("publishTimestamp"));
                LOG.infof("session %s time: %s ms", sessionID, (ChronoUnit.MILLIS.between(inicio, now)));
                LOG.infof("inicio %s now: %s ms", t.headers().get("publishTimestamp"), now);
                
                try {
//                	String msg = t.body();
//            		MsgType identifyType = quickfix.Message.identifyType(msg);
//            		//identifyType pode ser util um dia
//            		quickfix.Message message = new quickfix.Message();
//            		message.fromString(msg, null, false);
//					Session.sendToTarget(message, sessionID);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        };
		vertx.eventBus().consumer("quotas", consumer);
	}
}
