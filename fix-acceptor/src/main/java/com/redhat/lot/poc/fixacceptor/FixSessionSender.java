package com.redhat.lot.poc.fixacceptor;

import java.time.LocalDateTime;

import quickfix.FieldNotFound;
import quickfix.InvalidMessage;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.StringField;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;



public class FixSessionSender implements Runnable {

	private int currentIndex = 0;
	double currentLoop = 0;
	CircularList list = CircularList.getInstance();
	private SessionID sessionID;
	final String dif = "Diferencia msg ";

	private double cant_messages=0;
	private boolean active = true;
	
	private KafkaProducer<String, String> producer;
	
	public FixSessionSender(SessionID sessionID) {
		this.sessionID = sessionID;
		this.currentIndex = CircularList.getInstance().getIndex();
		this.currentLoop = CircularList.getInstance().getCurrentLoop();
		System.out.println(">>> FixSessionSender creado para sessionID " + sessionID.getSenderCompID());

	public FixSessionSender(SessionID sessionID, KafkaProducer<String, String> producer) {
		this.sessionID = sessionID;
		this.producer = producer;
		System.out.println(">>> FixSessionSender created with SessionID " + sessionID.getSenderCompID() + " and Kafka Producer");
	}


	@Override
	public void run() {
		
		System.out.println(">>> Thread ID: "+Thread.currentThread().getId()+", SessionID: "+sessionID);
		
		Message fixMessage;
		StringField stringField = new StringField(56,sessionID.toString());
		
		while (isActive()) {
			try {

				if (estoy_sincronizado_con_la_lista_circular()) 	{
					String msg = list.getStr(currentIndex);
					
					if (msg != null) {
						fixMessage = new Message();
						
						fixMessage.fromString(msg, null, false);
						
						
						// es necesario clonar dado a que los mensajes en la lista son instancias que deben ser 
						// modificadas para ser enviadas con valores propios de cada initiator (TargetCompID)
						//fixMessage = (Message) msg.clone();
						fixMessage.getHeader().setField(stringField);
						//System.out.println("Sender CID: "+sessionID.getSenderCompID()+", Target CID: "+sessionID.getTargetCompID());
						
						Session.sendToTarget(fixMessage, sessionID);
            
						try { 

							
							// add this message metrics
							Metrics.getInstance().addMetric(sessionID.toString(), fixMessage.getUtcTimeStamp(60), java.time.LocalDateTime.now());
		
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						
					}
					
					avanzar_punteros_a_lista();
				} else if (ya_consumi_todos_los_mensajes_de_la_lista()) {
					Thread.currentThread().sleep(1);

//					System.out.println("cant messages para " + sessionID.toString() + " = " + cant_messages);
					
				} else if (!estoy_sincronizado_con_la_lista_circular()){
          
					System.out.println("Perdi por más de 1 vuelta!!! currentLoop=" + currentLoop + " lista "
							+ list.getCurrentLoop() + ", current index=" + currentIndex + ", lista index=" + list.getIndex() );
					//break;
				}
			} catch (SessionNotFound | InterruptedException | InvalidMessage e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private boolean shouldSendToKafka(){
		if (this.producer != null){
			System.out.println("-------> Will send to Kafka");
			return true;
		}else{
			System.out.println("-------> Will not send to Kafka");
			return false;
		}
	}
	

	private boolean ya_consumi_todos_los_mensajes_de_la_lista() {
		return (currentLoop == list.getCurrentLoop()) && (currentIndex == list.getIndex());
	}

	private void avanzar_punteros_a_lista() {
		currentIndex++;
//		if (list.getIndex() - currentIndex > 10000 ) {
//			System.out.println("Diferencia session " + sessionID.toString() + " con respecto a la lista es " + (list.getIndex() - currentIndex) );
//		}
		if (currentIndex == CircularList.MAX) {
			currentIndex = 0;
			currentLoop++;
			System.out.println("llegue al MAX");
		}
	}

	private boolean estoy_sincronizado_con_la_lista_circular() {
		return (currentLoop == list.getCurrentLoop()) && (currentIndex < list.getIndex()) // estoy misma vuelta 
				|| ((currentLoop == list.getCurrentLoop() - 1 && currentIndex >= list.getIndex())); //estoy 1 vuela antes
	}

	
	public void stop() {
		setActive(false);
		Metrics.getInstance().remove(sessionID.toString());
		System.out.println("Stopped FIXSessionSender for " + sessionID.toString());

	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
