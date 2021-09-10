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

	MarketDataGenerator generator;

	@Inject
	Metrics metrics;

	@GET
    @Produces(MediaType.APPLICATION_JSON)
    public String generate(
    		@QueryParam(value = "duration") @DefaultValue(value = "10000") Integer duration,
    		@QueryParam(value = "cantmsgs") @DefaultValue(value = "1") Integer cantmsgs,
    		@QueryParam(value = "isKafka") @DefaultValue(value = "false") Boolean isKafka
    		) {
        
    	log.info((">>> Generating: Duration["+duration+"], Cant_msgs_ms["+cantmsgs+"], IsKafka["+isKafka.toString()+"]"));
        
		generator = new MarketDataGenerator(cantmsgs, duration, isKafka, metrics);
		generator.setPlay(true);
    	
		if(isKafka) {
			generator.setKafkaProducer(producer);
		}

		Thread t = new Thread(generator);
		t.start();

    	return "{'status' : 'STARTED',  'duration: '"+duration+"','cantmsgs: '"+cantmsgs+"', 'toKafka' : '"+isKafka.toString()+"'}";
	}


	@POST
	public long publishToKafka(@QueryParam("key") String key, @QueryParam("value") String value) throws InterruptedException, ExecutionException, TimeoutException {
		return producer.send(new ProducerRecord<>("test", "{status:OK}")).get(5, TimeUnit.SECONDS).offset();
	}

	@GET
	@Path("/stop")
	@Produces(MediaType.APPLICATION_JSON)
	public String stop() {
		generator.stop();
		return "{'status' : 'STOPED'}";
	}
	
}
