package com.redhat.lot.poc.fixacceptor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import quickfix.DoubleField;
import quickfix.InvalidMessage;
import quickfix.Message;

@ApplicationScoped
public class MarketDataGeneratorKafka implements Runnable{

	@Inject
	Logger log;

	private final static String msg = "8=FIX.4.49=12835=D34=449=BANZAI52=20210715-21:06:54.41656=EXEC11=162638321441821=138=340=154=155=VALE59=060=20210715-21:06:54.41610=015";
	
	private boolean play = true;
	private int quantity = 100;
	private int interval = 1000;
	private long time;
	private int duration;
	private long initPerSecondTime;
	private long currenttime;
	private long totalMessagesGenerated;
	private int cycles;
	
	private boolean isKafka;
	private static int printLogInterval = 10000;// interval in miliseconds (used just for kafka logs)
	private long lastPrintLog;// (used just for kafka logs)
	private double[][] metrics;// (used just for kafka logs)
	public static final int MAX = 250000;// (used just for kafka logs)
	private Integer totalMessages;// (used just for kafka logs)
	private Emitter<String> emitter;
	
	
	public Emitter<String> getEmitter() {
		return emitter;
	}

	public void setEmitter(Emitter<String> emitter) {
		this.emitter = emitter;
	}

	public boolean isKafka() {
		return isKafka;
	}

	public void setKafka(boolean isKafka) {
		this.isKafka = isKafka;
	}

	//end time execution, since initTime (inittime + (duration in milliseconds))
	private long endTime;

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	
	public MarketDataGeneratorKafka(int quantity, int interval, int duration, boolean isKafka) {
		this.interval = interval;
		this.quantity = quantity;
		this.duration = duration;
		this.isKafka = isKafka;
	}
	
	public MarketDataGeneratorKafka() {
	}

	@Override
	public void run() {
		
		System.out.println(">>> Arrancando Envio de Mensajes hacia Kafka " + quantity);
		
		metrics = new double[MAX][2];
		lastPrintLog = System.currentTimeMillis();
		
		// End Time = Time until the thread will be executed
		endTime = System.currentTimeMillis() + duration;
		
		currenttime = System.currentTimeMillis();
		
		cycles = 0;
		totalMessages = 0;
		totalMessagesGenerated = 0;
		
		while (play) {
			System.out.println(">>> Ciclo: "+cycles);
			generateMarketData();
		}

	}

	public void generateMarketData() {
		
		cycles = cycles + 1;
		initPerSecondTime = System.currentTimeMillis();
		long tiempo_restante_loop = 0;
		time = System.currentTimeMillis();
		
		for (int i = 0; i <= quantity; i++) {
			
			// send a message to Kafka
			sentToKafka();
			
			if (System.currentTimeMillis() - time >= interval) {
				System.out.println(
						"**ATENCION!!** Tiempo excedido para ciclo generación de market data en el intervalo. Generado "
								+ quantity);
				break;
			}
			

		}
		
		printKafkaLogs();
		
		tiempo_restante_loop = interval - (initPerSecondTime - currenttime);
		if (tiempo_restante_loop > 0) {
			esperar(tiempo_restante_loop);
		}
		
		totalMessagesGenerated = totalMessagesGenerated + quantity;
		currenttime = System.currentTimeMillis();
		
		if(currenttime>=endTime){
			stop();
			printKafkaLogs();
		}
		
	}

	public Message generateMessage(){
		Message fixMessage = new Message();
		Long timestamp = System.currentTimeMillis();
//		fixMessage.setField(new DoubleField(60, timestamp.doubleValue()));
		

		try {
			fixMessage.fromString(msg, null, false);
			fixMessage.setField(new DoubleField(60,  timestamp));
		} catch (InvalidMessage e) {
			e.printStackTrace();
		}

		return fixMessage;
	}

	private void esperar(long tiempo_restante_loop) {
		try {
//			Thread.currentThread();
//			Thread.sleep(tiempo_restante_loop);
			System.out.println(">>> Esperando "+tiempo_restante_loop+" ms");
			Thread.currentThread().sleep(tiempo_restante_loop);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		System.out.println((">>> Time is up ("+duration+" ms)! Stopping thread..."));
		System.out.println((">>> Total Messages Generated... ("+totalMessagesGenerated+")"));
		play = false;
	}
	
	private void sentToKafka() {
		try {
			Message message = generateMessage();
			
			// send message to kafka broker...
			System.out.println("Message: "+message.toString());
			emitter.send(message.toString());
			
			metrics[totalMessages][0] = System.currentTimeMillis();
			metrics[totalMessages][1] = message.getDouble(60);
			
			totalMessages=totalMessages+1;
			
		}catch(Exception e) {
			System.out.println("[ERROR] No se pudo enviar el mensaje al Broker de Kafka.");
			e.printStackTrace();
		}
		
		totalMessagesGenerated = totalMessagesGenerated + 1;
		
		
	}
	
	private void printKafkaLogs() {
		
		// ¿es necesario imprimir logs de resumen de metricas? (basado en un intervalo de tiempo [printLogInterval])
//		if( ( lastPrintLog + printLogInterval ) <= System.currentTimeMillis() ) {
//			lastPrintLog = System.currentTimeMillis();
			
			// log metrics if there is one...
			if(metrics[1]!=null) {
				// System.out.println(">>> Printing Metrics ...");
				
				double durationMin=-1;
				double durationMax=-1;
				double durationMedian=-1;
				
				double end;
				double start;
				double val;
				
				for(int j=1; j<=metrics.length && metrics[j][0]!=0.0d;j++) {
					
					end = (Double) metrics[j][0];
					start = (Double) metrics[j][1];
					val = end-start;
					
					//System.out.println(">>> Kafka Metric ["+j+"] "+val);
					
					if(durationMin<0 || val<durationMin)
						durationMin=val;
					else if(durationMax<0 || val>durationMax)
						durationMax=val;
					
					durationMedian=durationMedian+val;
				}
				
				durationMedian=durationMedian>0?durationMedian/totalMessagesGenerated:durationMedian;
				
				//print metrics
				if(durationMax>=0){
					System.out.println((">>> Total Messages sent to Kafka #["+totalMessages+"]"));
					System.out.println(">>> Metrics Resume for Kafka Messages sent: Total["+totalMessagesGenerated+"], Duration: max["+durationMax+"], min["+durationMin+"], med["+durationMedian+"]");
				}
				
				// reseting metrics 
				metrics = new double[MAX][2];
				totalMessages = 0;
			}
//		}
	}

}
