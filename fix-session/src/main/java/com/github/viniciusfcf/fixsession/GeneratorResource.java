package com.github.viniciusfcf.fixsession;

import java.time.LocalDateTime;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.mutiny.core.eventbus.EventBus;

@Path("/generator")
public class GeneratorResource {

    @Inject
    EventBus eventBus;

    @Inject
    Logger log;

    @Inject
    ManagedExecutor managedExecutor;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String generate(@QueryParam(value = "sizePerThread") @DefaultValue(value = "1000") Integer size, @QueryParam(value = "threads") @DefaultValue(value = "1") Integer threads) {
        log.info(("------------starting generation------"));
        for (int i = 0; i < threads; i++) {
            Supplier<Integer> publisher = new Supplier<Integer>() {
                public Integer get() {
                    long inicio = System.currentTimeMillis();
                    for (int i = 0; i < size; i++) {
                        eventBus.publish("quotas", "Quota: "+i, new DeliveryOptions().addHeader("publishTimestamp", LocalDateTime.now().toString()));
                    }
                    long fim = System.currentTimeMillis();
                    log.infof("Time to publish: %s ms", (fim-inicio));
                    return 0;
                }
            };
            managedExecutor.supplyAsync(publisher);
            
        }

        return "OK";
    }
}