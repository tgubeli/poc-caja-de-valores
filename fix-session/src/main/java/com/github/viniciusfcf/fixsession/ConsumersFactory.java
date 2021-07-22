package com.github.viniciusfcf.fixsession;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import io.quarkus.runtime.StartupEvent;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;

/**
 * FIXME delete it after FIX protocol is working
 */
@ApplicationScoped
public class ConsumersFactory {
    
    @Inject
    EventBus eventBus;
    
    @Inject
    public static final Logger LOG = Logger.getLogger(ConsumersFactory.class);

    public void start(@Observes StartupEvent startupEvent) {
        LOG.info(("Init consumers"));
        Consumer<Message<Integer>> consumer = new Consumer<Message<Integer>>() {
            public void accept(Message<Integer> t) {

                LocalDateTime inicio = LocalDateTime.parse(t.headers().get("publishTimestamp"));
                LocalDateTime now = LocalDateTime.now();
                LOG.infof("Msg consumed: %s ms", (ChronoUnit.MILLIS.between(inicio, now)));
            }
        };
        //add 2 consumers
        eventBus.localConsumer("quotas", consumer);
        eventBus.localConsumer("quotas", consumer);
    }
}
