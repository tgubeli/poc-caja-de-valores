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
                @Override
				public Integer get() {
                    long inicio = System.currentTimeMillis();
                    for (int i = 0; i < size; i++) {
                        String msg = "8=FIX.4.49=12835=D34=449=BANZAI52=20210715-21:06:54.41656=EXEC11=162638321441821=138=340=154=155=VALE59=060=20210715-21:06:54.41610=015";
                        DeliveryOptions deliveryOptions = new DeliveryOptions();
						String string = LocalDateTime.now().toString();
						deliveryOptions.addHeader("publishTimestamp", string);
						eventBus.publish("quotas", msg, deliveryOptions);
                    }
                    long fim = System.currentTimeMillis();
                    log.infof("Time to publish: %s ms", (fim-inicio));
                    return 0;
                }
            };
            managedExecutor.supplyAsync(publisher);
            
        }

        return "{status:OK, threads: "+threads+", messagesPerThread: "+size+", totalMessagesGenerated: "+threads*size+"}";
    }
}