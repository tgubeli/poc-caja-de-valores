package com.redhat.lot.poc.fixacceptor;

import java.time.Duration;
import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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

@ApplicationScoped
public class ServerApplicationAdapter implements quickfix.Application {
	
	public static final Logger log = Logger.getLogger(ServerApplicationAdapter.class);

    @Inject
    KafkaConsumer<String, String> consumer;

    @ConfigProperty(name = "kafka.topic")
	String kafkaTopic;


    @Override
	public void onCreate(SessionID sessionID) {
        log.info("--------- onCreate ---------");
        // Session.lookupSession(sessionID).getLog().onEvent("Valid order types: " + validOrderTypes);
    }

    @Override
	public void onLogon(SessionID sessionID) {
        log.info("--------- onLogon ---------");
        log.info("--------- Starting kafka consumer Thread ---------");
       
        //FIXME TODO aqui deveria criar um consumer novo, e nao reutilizar...
        consumer.subscribe(Collections.singleton(kafkaTopic));
        new Thread( new ConsumerRunnable(sessionID, consumer)).start();
        
        
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
			log.info("----INICIANDO THREAD--------"+session.isEnabled());
			while(session.isLoggedOn()) {
				
				final ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofSeconds(1));
	            consumerRecords.forEach(record -> {
	                System.out.printf("Polled Record:(%s, %s, %d, %d)\n",
	                            record.key(), record.value(),
	                            record.partition(), record.offset());
	
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
