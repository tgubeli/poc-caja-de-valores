package com.redhat.lot.poc.fixacceptor;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.jboss.logging.Logger;

import quickfix.Message;

@Path("/generator")
public class GeneratorResource {

    @Inject
    Logger log;

    @Inject
    ManagedExecutor managedExecutor;

    //TODO Cambiar este parametro estatico a un properties o algo asi
    @Channel("marketdata")
    @OnOverflow(value = OnOverflow.Strategy.BUFFER, bufferSize = 10000)
    Emitter<String> emitter;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String generate(
    		@QueryParam(value = "sizePerThread") @DefaultValue(value = "10") Integer size, 
    		@QueryParam(value = "threads") @DefaultValue(value = "1") Integer threads,
    		@QueryParam(value = "interval") @DefaultValue(value = "1000") Integer interval,
    		@QueryParam(value = "duration") @DefaultValue(value = "10000") Integer duration,
    		@QueryParam(value = "chunks") @DefaultValue(value = "1") Integer chunks,
    		@QueryParam(value = "isKafka") @DefaultValue(value = "false") Boolean isKafka
    		) {
        
    	log.info((">>> Generating: Threads["+threads+"], MessagesPerThread["+size+"], Interval["+interval+"], Duration["+duration+"], Chunks["+chunks+"], IsKafka["+isKafka.toString()+"]"));
        
    	//for(int i=0;i<threads;i++) {
    	if(!isKafka) {
    		
    		MarketDataGenerator generator = MarketDataGenerator.getInstance();
	        generator.setQuantity(size);
	        generator.setInterval(interval);
	        generator.setDuration(duration);
	        generator.setChunks(chunks);
	        generator.setPlay(true);
	        
	        Thread t = new Thread(generator);
	        
	        t.start();
    	}else {
    		
    		MarketDataGeneratorKafka generator = new MarketDataGeneratorKafka(size,interval,duration,isKafka);
			
    		generator.setEmitter(emitter);
    		
    		Thread t = new Thread(generator);
 	        t.start();

    	}
    		
    	

        return "{'status' : 'STARTED', 'threads' : '"+threads+"', 'messagesPerThread' : '"+size+"', 'interval' : '"+interval+"', 'duration: '"+duration+"','chunks: '"+chunks+"', 'toKafka' : '"+isKafka.toString()+"'}";
      }

<<<<<<< HEAD
	@GET
	@Path("/kafka")
	@Produces(MediaType.TEXT_PLAIN)
	public String publishToKafka() {
		emitter.send(MarketDataGenerator.generateMessage());

		return "{status:OK}";
	}
=======
	  @GET
	  @Path("/stop")
	  @Produces(MediaType.APPLICATION_JSON)
	  public String stop() {
		  
		  MarketDataGenerator.getInstance().stop();
		  return "{'status' : 'STOPED'}";
		  
	  }
>>>>>>> 562f1488c537419c1d9a237a8b9bd1b6ee98f452
  
}

