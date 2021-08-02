package com.redhat.lot.poc.fixacceptor;

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
    Logger log;

    @Inject
    ManagedExecutor managedExecutor;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String generate(@QueryParam(value = "sizePerThread") @DefaultValue(value = "1000") Integer size, @QueryParam(value = "interval") @DefaultValue(value = "100") Integer interval) {
        log.info(("------------starting generation------"));
        
        MarketDataGenerator generator = new MarketDataGenerator();
        generator.setQuantity(size);
        generator.setInterval(interval);
        
        Thread t = new Thread(generator);
        
        t.start();


        return "{status:OK}";
  }
}