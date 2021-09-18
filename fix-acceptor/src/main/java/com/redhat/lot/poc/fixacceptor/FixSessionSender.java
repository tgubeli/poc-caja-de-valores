package com.redhat.lot.poc.fixacceptor;

import quickfix.FieldNotFound;
import quickfix.InvalidMessage;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.StringField;

public class FixSessionSender implements Runnable {

	private int currentIndex = 0;
	double currentLoop = 0;
	CircularList list = CircularList.getInstance();
	private SessionID sessionID;
	final String dif = "Diferencia msg ";
	Metrics metrics;

	private boolean active = true;
	
	public FixSessionSender(SessionID sessionID, Metrics metrics) {
		this.sessionID = sessionID;
		this.metrics = metrics;
		System.out.println(">>> FixSessionSender created with SessionID " + sessionID.getSenderCompID());
	}

	@Override
	public void run() {
		
		System.out.println(">>> Thread ID: "+Thread.currentThread().getId()+", SessionID: "+sessionID);
		
		Message fixMessage;
		StringField stringField = new StringField(56,sessionID.toString());
		
		while (true) {
			try {

				if (estoy_sincronizado_con_la_lista_circular()) 	{
					String msg = list.getStr(currentIndex);

					fixMessage = new Message();
					fixMessage.fromString(msg, null, false);
					
					// add this message metrics
					metrics.addMetric(sessionID.toString(), fixMessage.getUtcTimeStamp(60), java.time.LocalDateTime.now());
					
					// es necesario clonar dado a que los mensajes en la lista son instancias que deben ser 
					// modificadas para ser enviadas con valores propios de cada initiator (TargetCompID)
					//fixMessage = (Message) msg.clone();
					fixMessage.getHeader().setField(stringField);

					Session.sendToTarget(fixMessage, sessionID);
					
					avanzar_punteros_a_lista();
				} else if (ya_consumi_todos_los_mensajes_de_la_lista()) {
					Thread.currentThread().sleep(1);
				} else {
					System.out.println("Perdi por más de 1 vuelta!!! currentLoop=" + currentLoop + " lista "
							+ list.getCurrentLoop());
					break;
				}
			} catch (SessionNotFound | InterruptedException | FieldNotFound | InvalidMessage e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private boolean ya_consumi_todos_los_mensajes_de_la_lista() {
		return (currentLoop == list.getCurrentLoop()) && (currentIndex == list.getIndex());
	}

	private void avanzar_punteros_a_lista() {
		currentIndex++;
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
		metrics.remove(sessionID.toString());
		System.out.println("Stopped FIXSessionSender for " + sessionID.toString());

	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}


}
