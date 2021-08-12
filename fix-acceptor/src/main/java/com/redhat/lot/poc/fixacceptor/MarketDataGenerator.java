package com.redhat.lot.poc.fixacceptor;

import java.text.SimpleDateFormat;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import quickfix.DoubleField;
import quickfix.InvalidMessage;
import quickfix.Message;

@ApplicationScoped
public class MarketDataGenerator implements Runnable {

	@Inject
	Logger log;

	private final static String msg = "8=FIX.4.49=12835=D34=449=BANZAI52=20210715-21:06:54.41656=EXEC11=162638321441821=138=340=154=155=VALE59=060=20210715-21:06:54.41610=015";
	private String fixDatePattern = "YYYYMMDD-HH:mm:ss.SSS";
	private SimpleDateFormat simpleDateFormat;
	private boolean play = true;
	private int quantity = 100;
	private int interval = 1000;
	private long time;
	private int duration;
	private long initPerSecondTime;
	private long currenttime;
	private long totalMessagesGenerated;
	private int cycles;
	

	private Emitter<Message> emitter;
	
	
	public Emitter<Message> getEmitter() {
		return emitter;
	}

	public void setEmitter(Emitter<Message> emitter) {
		this.emitter = emitter;
	}

	//end time execution, since initTime (inittime + (duration in milliseconds))
	private long endTime;

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public void setPlay(boolean play) {
		this.play = play;
	}
	
	public MarketDataGenerator(int quantity, int interval, int duration) {
		this.interval = interval;
		this.quantity = quantity;
		this.duration = duration;
	}
	
	public MarketDataGenerator() {
	}

	@Override
	public void run() {
		System.out.println(">>> Arrancando " + quantity);
		
		cycles = 0;
		totalMessagesGenerated = 0;
		currenttime = System.currentTimeMillis();
		
		// End Time = Time until the thread will be executed
		endTime = currenttime + duration;
		
		simpleDateFormat = new SimpleDateFormat(fixDatePattern);
		
		while (play) {
			generateMarketData();
		}

	}

	public void generateMarketData() {
		
		cycles = cycles + 1;
		
		//System.out.println((">>> Ciclo: "+cycles));
		
		initPerSecondTime = System.currentTimeMillis();
		
        Long timestamp;
		
		
		long tiempo_restante_loop = 0;
		double d;
		
		time = System.currentTimeMillis();
		
		for (int i = 0; i <= quantity; i++) {
			
			CircularList.getInstance().insert(MarketDataGenerator.generateMessage());
			
			if (System.currentTimeMillis() - time >= interval) {
				System.out.println(
						"**ATENCION!!** Tiempo excedido para ciclo generación de market data en el intervalo. Generado "
								+ quantity);
				break;
			}

		}

		//System.out.println((">>> Generado " + quantity + " mensajes en "+interval+" milisegundos"));
		
		tiempo_restante_loop = interval - (initPerSecondTime - currenttime);
		if (tiempo_restante_loop > 0) {
			esperar(tiempo_restante_loop);
		}
		
		totalMessagesGenerated = totalMessagesGenerated + quantity;
		currenttime = System.currentTimeMillis();
		if(currenttime>=endTime)
			stop();

	}

	public static Message generateMessage(){
		Message fixMessage = new Message();
		Long timestamp = System.currentTimeMillis();

		try {
			fixMessage.fromString(msg, null, false);
			fixMessage.setField(new DoubleField(60, timestamp ));
		} catch (InvalidMessage e) {
			e.printStackTrace();
		}

		return fixMessage;
	}

	private void esperar(long tiempo_restante_loop) {
		try {
//			Thread.currentThread();
//			Thread.sleep(tiempo_restante_loop);
			
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
	

}
