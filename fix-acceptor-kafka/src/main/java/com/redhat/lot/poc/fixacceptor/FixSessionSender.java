// package com.redhat.lot.poc.fixacceptor;

// import quickfix.FieldNotFound;
// import quickfix.Message;
// import quickfix.Session;
// import quickfix.SessionID;
// import quickfix.SessionNotFound;
// import quickfix.StringField;

// public class FixSessionSender implements Runnable {

// 	private int currentIndex = 0;
// 	double currentLoop = 0;
// 	CircularList list = CircularList.getInstance();
// 	private SessionID sessionID;
// 	final String dif = "Diferencia msg ";
	

// 	public FixSessionSender(SessionID sessionID) {
// 		super();
// 		this.sessionID = sessionID;
// 		System.out.println(">>> FixSessionSender creado para sessionID " + sessionID.getSenderCompID());
// 	}

// 	@Override
// 	public void run() {
		
// 		System.out.println(">>> Thread ID: "+Thread.currentThread().getId()+", SessionID: "+sessionID);
		
// 		Message fixMessage;
// 		StringField stringField = new StringField(56,sessionID.toString());
		
// 		while (true) {
// 			try {

// 				if (estoy_sincronizado_con_la_lista_circular()) 	{
					
// 					Message msg = list.get(currentIndex);
					
// 					// add this message metrics
// 					Metrics.getInstance().addMetric(sessionID.toString(), msg.getUtcTimeStamp(60), java.time.LocalDateTime.now());
					
// 					// es necesario clonar dado a que los mensajes en la lista son instancias que deben ser 
// 					// modificadas para ser enviadas con valores propios de cada initiator (TargetCompID)
// 					fixMessage = (Message) msg.clone();
// 					fixMessage.getHeader().setField(stringField);

// 					Session.sendToTarget(msg, sessionID);
					
// 					avanzar_punteros_a_lista();
					
					
// 				} else if (ya_consumi_todos_los_mensajes_de_la_lista()) {
					
					
// 					Thread.currentThread().sleep(1);
					
// 				} else {
// 					System.out.println("Perdi por más de 1 vuelta!!! currentLoop=" + currentLoop + " lista "
// 							+ list.getCurrentLoop());
// 					break;
// 				}
// 			} catch (SessionNotFound | FieldNotFound | InterruptedException e) {
// 				// TODO Auto-generated catch block
// 				e.printStackTrace();
// 			}
// 		}


// 	}
	

// 	private boolean ya_consumi_todos_los_mensajes_de_la_lista() {
// 		return (currentLoop == list.getCurrentLoop()) && (currentIndex == list.getIndex());
// 	}

// 	private void avanzar_punteros_a_lista() {
// 		currentIndex++;
// 		if (currentIndex == CircularList.MAX) {
// 			currentIndex = 0;
// 			currentLoop++;
// 			System.out.println("llegue al MAX");
// 		}
// 	}

// 	private boolean estoy_sincronizado_con_la_lista_circular() {
// 		return (currentLoop == list.getCurrentLoop()) && (currentIndex < list.getIndex()) // estoy misma vuelta 
// 				|| ((currentLoop == list.getCurrentLoop() - 1 && currentIndex >= list.getIndex())); //estoy 1 vuela antes
// 	}

// }
