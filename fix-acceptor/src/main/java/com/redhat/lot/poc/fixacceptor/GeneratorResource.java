package com.redhat.lot.poc.fixacceptor;

import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import quickfix.Message;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

@Path("/generator")
public class GeneratorResource {

    @Inject
    Logger log;

    @Inject
    ManagedExecutor managedExecutor;

    @Channel("marketdata")
    Emitter<Message> emitter;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String generate(
    		@QueryParam(value = "sizePerThread") @DefaultValue(value = "10") Integer size, 
    		@QueryParam(value = "threads") @DefaultValue(value = "1") Integer threads,
    		@QueryParam(value = "interval") @DefaultValue(value = "1000") Integer interval,
    		@QueryParam(value = "duration") @DefaultValue(value = "10000") Integer duration
    		) {
        
    	log.info((">>> Generating: Threads["+threads+"], MessagesPerThread["+size+"], Interval["+interval+"], Duration["+duration+"]"));
        
    	//for(int i=0;i<threads;i++) {
    	
	        MarketDataGenerator generator = new MarketDataGenerator();
	        generator.setQuantity(size);
	        generator.setInterval(interval);
	        
	        Thread t = new Thread(generator);
	        
	        t.start();
    	//}

        return "{'status' : 'DONE', 'threads' : '"+threads+"', 'messagesPerThread' : '"+size+"', 'interval' : '"+interval+"', 'duration: '"+duration+"'}";
  }

  @GET
  @Path("/publish-to-kafka")
  @Produces(MediaType.TEXT_PLAIN)
  public String publishToKafka() {

			emitter.send(MarketDataGenerator.generateMessage());

      return "{status:OK}";
  }
  
}