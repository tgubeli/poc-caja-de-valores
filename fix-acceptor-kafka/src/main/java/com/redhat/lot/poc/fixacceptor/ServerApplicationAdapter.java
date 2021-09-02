package com.redhat.lot.poc.fixacceptor;

import java.util.HashMap;
import java.util.Map;
import java.time.Duration;
import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.InvalidMessage;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.UnsupportedMessageType;

import org.apache.kafka.common.serialization.StringDeserializer;



@ApplicationScoped
public class ServerApplicationAdapter implements quickfix.Application {
	
	public static final Logger log = Logger.getLogger(ServerApplicationAdapter.class);

    @ConfigProperty(name = "kafka.topic")
	String kafkaTopic;

    @ConfigProperty(name = "kafka.bootstrap.servers")
	String kafkaServer;

    @ConfigProperty(name = "kafka.auto.offset.reset")
	String offSetReset;

    boolean done = false;
    volatile String last;

    @Override
	public void onCreate(SessionID sessionID) {
        log.info("--------- onCreate ---------");
        // Session.lookupSession(sessionID).getLog().onEvent("Valid order types: " + validOrderTypes);
    }

    @Override
	public void onLogon(SessionID sessionID) {
        log.info("--------- onLogon ---------");
        KafkaConsumer<String, String> consumer = buildConsumer(sessionID.toString());
        consumer.subscribe(Collections.singleton(kafkaTopic));
        new Thread( new ConsumerRunnable(sessionID, consumer)).start();
    }

    private KafkaConsumer<String, String> buildConsumer(String session){
        Map<String, Object> config = new HashMap<String, Object>();

        String groupId = session.substring(session.lastIndexOf(">")+1);
        config.put("group.id", groupId);
        config.put("topic", kafkaTopic);
        config.put("bootstrap.servers", kafkaServer);
        config.put("auto.offset.reset", offSetReset);

        log.info("Starting kafka consumer");
        log.info("\t group.id: "+groupId);
        log.info("\t topic: "+kafkaTopic);
        log.info("\t bootstrap.servers: "+kafkaServer);
        log.info("\t auto.offset.reset: "+offSetReset);

        return new KafkaConsumer<>(config, new StringDeserializer(), new StringDeserializer());
    }
    
    static class ConsumerRunnable implements Runnable {

        private SessionID sessionID;
		private Session session;
		private KafkaConsumer<String, String> consumer;

		public ConsumerRunnable(SessionID sessionID, KafkaConsumer<String, String> consumer) {
			this.sessionID = sessionID;
			this.session = Session.lookupSession(sessionID);
    		this.consumer = consumer;
    	}
    	
		@Override
		public void run() {
			while(session.isLoggedOn()) {
				
				final ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofSeconds(1));
	            consumerRecords.forEach(record -> {
	                log.info("Polled Record:");
                    log.info("\t record.key: "+record.key()+" record.value: "+record.value());
                    log.info("\t record.partition: "+record.partition()+" record.offset: "+record.offset());
	
	                try {
	                    Message fixMessage = new Message();
	                    fixMessage.fromString(record.value(), null, false);
	                    Session.sendToTarget(fixMessage, sessionID);
	                } catch (InvalidMessage e) {
	                	log.info("Erro ao enviar", e);
	                    e.printStackTrace();
	                } catch (SessionNotFound e) {
	                    // TODO Auto-generated catch block
	                    e.printStackTrace();
	                }
	                
	            });

			}
			log.info("=======Matando a Thread: "+sessionID);
			consumer.close();
		}
    	
    }

    @Override
	public void onLogout(SessionID sessionID) {
        log.info("--------- onLogout ---------");
    }

    @Override
	public void toAdmin(quickfix.Message message, SessionID sessionID) {
        log.info("--------- toAdmin ---------");
    }

    @Override
	public void toApp(quickfix.Message message, SessionID sessionID) throws DoNotSend {
        //log.info("--------- toApp ---------");
    }

    @Override
	public void fromAdmin(quickfix.Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat,
            IncorrectTagValue, RejectLogon {
        log.info("--------- fromAdmin ---------");
    }

    @Override
	public void fromApp(quickfix.Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat,
            IncorrectTagValue, UnsupportedMessageType {
        log.info("--------- fromApp ---------");
        
    }

}
