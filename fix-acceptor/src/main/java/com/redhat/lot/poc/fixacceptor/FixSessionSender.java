package com.redhat.lot.poc.fixacceptor;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;

public class FixSessionSender implements Runnable {

	private int currentIndex = 0;
	double currentLoop = 0;
	CircularList list = CircularList.getInstance();
	private SessionID sessionID;
	final String dif = "Diferencia msg ";
	private long lastPrintLog;
	private double durationMin;
	private double durationMax;
	private double durationMedian;
	private Integer totalMessages;
	public static final int MAX = 250000;
	private double[][] metrics;

	public FixSessionSender(SessionID sessionID) {
		super();
		this.sessionID = sessionID;
		lastPrintLog = System.currentTimeMillis();
		totalMessages = 0;
		System.out.println(">>> FixSessionSender creado para sessionID " + sessionID.getSenderCompID());
	}

	@Override
	public void run() {
		double d;
		Long timestamp;
		int printLogInterval = 10000;// interval in miliseconds
		
		metrics = new double[MAX][2];
		
		while (true) {
			try {

				if (estoy_sincronizado_con_la_lista_circular()) 	{
					Message msg = list.get(currentIndex);
					
					metrics[totalMessages][0] = System.currentTimeMillis();
					metrics[totalMessages][1] = msg.getDouble(60);
					
					//timestamp = System.currentTimeMillis();
					//d = timestamp.doubleValue() - msg.getDouble(60);
					//System.out.println(dif + d); // TODO capturar cada n lineas los percentiles

					Session.sendToTarget(msg, sessionID);
					
					totalMessages=totalMessages+1;
					
					avanzar_punteros_a_lista();
					
					// si se han enviado MUCHOS mensajes y aun quedan por procesar, revisar si es hora de imprimir un log
					if(totalMessages>200) {
						// es necesario imprimir logs de resumen de metricas? (basado en un intervalo de tiempo [printLogInterval])
						if( ( lastPrintLog + printLogInterval ) <= System.currentTimeMillis() ) {
							lastPrintLog = System.currentTimeMillis();
							// log metrics if there is one...
							if(metrics[1]!=null)
								printMetrics(); 
						}
					}
					
				} else if (ya_consumi_todos_los_mensajes_de_la_lista()) {
					
					// es necesario imprimir logs de resumen de metricas? (basado en un intervalo de tiempo [printLogInterval])
					if( ( lastPrintLog + printLogInterval ) <= System.currentTimeMillis() ) {
						lastPrintLog = System.currentTimeMillis();
						// log metrics if there is one...
						if(metrics[1]!=null)
							printMetrics(); 
					}
					
					Thread.currentThread().sleep(1);
					
				} else {
					System.out.println("Perdi por más de 1 vuelta!!! currentLoop=" + currentLoop + " lista "
							+ list.getCurrentLoop());
					break;
				}
			} catch (SessionNotFound | FieldNotFound | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}
	
	private void printMetrics() {
		
		// System.out.println(">>> Printing Metrics ...");
		
		durationMin=-1;
		durationMax=-1;
		durationMedian=-1;
		
		double end;
		double start;
		String[] values;
		double val;
		
		for(int j=1; j<=metrics.length && metrics[j][0]!=0.0d;j++) {
			
			end = (Double) metrics[j][0];
			start = (Double) metrics[j][1];
			val = end-start;
			
			//System.out.println(">>> Metric ["+j+"] "+val);
			
			if(durationMin<0 || durationMin>val)
				durationMin=val;
			else if(durationMin<0 || durationMax<val)
				durationMax=val;
			
			durationMedian=durationMedian+val;
		}
		
		durationMedian=durationMedian>0?durationMedian/totalMessages:durationMedian;
		
		//print metrics
		if(durationMax>=0)
			System.out.println(">>> Metrics Resume for SessionID ["+sessionID+"]: max["+durationMax+"], min["+durationMin+"], med["+durationMedian+"]");
		
		// reseting metrics 
		metrics = new double[MAX][2];
		totalMessages = 0;
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

}
