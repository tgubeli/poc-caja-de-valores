package com.redhat.lot.poc.fixacceptor;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.redhat.lot.poc.fixacceptor.MarketDataGenerator;


@Path("/generator")
public class GeneratorResource {

    @Inject
    Logger log;

    @Inject
    ManagedExecutor managedExecutor;

	@Inject
	KafkaProducer<String, String> producer;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String generate(
    		@QueryParam(value = "sizePerThread") @DefaultValue(value = "10") Integer size, 
    		@QueryParam(value = "threads") @DefaultValue(value = "1") Integer threads,
    		@QueryParam(value = "interval") @DefaultValue(value = "1000") Integer interval,
    		@QueryParam(value = "duration") @DefaultValue(value = "10000") Integer duration,
    		@QueryParam(value = "chunks") @DefaultValue(value = "1") Integer chunks
    		) {
        
    	log.info((">>> Generating: Threads["+threads+"], MessagesPerThread["+size+"], Interval["+interval+"], Duration["+duration+"], Chunks["+chunks+"]"));
        
    	MarketDataGenerator generator = MarketDataGenerator.getInstance();
		generator.setQuantity(size);
		generator.setInterval(interval);
		generator.setDuration(duration);
		generator.setChunks(chunks);
		generator.setPlay(true);

		Thread t = new Thread(generator);
		t.start();

        return "{'status' : 'STARTED', 'threads' : '"+threads+"', 'messagesPerThread' : '"+size+"', 'interval' : '"+interval+"', 'duration: '"+duration+"','chunks: '"+chunks+"}";
	}

	@POST
	public long publishToKafka(@QueryParam("key") String key, @QueryParam("value") String value) throws InterruptedException, ExecutionException, TimeoutException {
		return producer.send(new ProducerRecord<>("test", "{status:OK}")).get(5, TimeUnit.SECONDS).offset();
	}

	@GET
	@Path("/stop")
	@Produces(MediaType.APPLICATION_JSON)
	public String stop() {
		MarketDataGenerator.getInstance().stop();
		return "{'status' : 'STOPED'}";
	}
  
}
