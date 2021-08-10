package com.redhat.lot.poc.fixacceptor;

import javax.inject.Inject;

import org.jboss.logging.Logger;

import quickfix.DoubleField;
import quickfix.InvalidMessage;
import quickfix.Message;

import java.util.concurrent.CompletableFuture;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

public class MarketDataGenerator implements Runnable {

	// Using the Emitter you are sending messages from your imperative code to reactive messaging. 
	// These messages are stored in a queue until they are sent. If the Kafka producer client can’t keep up with messages trying to be sent over to Kafka, 
	// this queue can become a memory hog and you may even run out of memory. You can use @OnOverflow to configure back-pressure strategy. 
	// It lets you configure the size of the queue (default is 256) and the strategy to apply when the buffer size is reached. 
	// Available strategies are DROP, LATEST, FAIL, BUFFER, UNBOUNDED_BUFFER and NONE.
	@Channel("marketdata")
    Emitter<Message> emitter;

	@Inject
	Logger log;

	private final String msg = "8=FIX.4.49=12835=D34=449=BANZAI52=20210715-21:06:54.41656=EXEC11=162638321441821=138=340=154=155=VALE59=060=20210715-21:06:54.41610=015";
	private boolean play = true;
	private int quantity = 100;
	private int interval = 1000;
	private long time;

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public void setPlay(boolean play) {
		this.play = play;
	}

	@Override
	public void run() {
		System.out.println("arrancando " + quantity);

		while (play) {
			generateMarketData();
		}

	}

	public void generateMarketData() {
		System.out.println(("------------generando------"));
		time = System.currentTimeMillis();
		Long timestamp;
		long tiempo_restante_loop = 0;
		double d;
		for (int i = 0; i <= quantity; i++) {
			Message fixMessage = new Message();
			timestamp = System.currentTimeMillis();
			d = timestamp.doubleValue();
			fixMessage.setField(new DoubleField(60, d));
			try {
				fixMessage.fromString(msg, null, false);
			} catch (InvalidMessage e) {
				e.printStackTrace();
			}
			CircularList.getInstance().insert(fixMessage);
			if (System.currentTimeMillis() - time >= interval) {
				System.out.println(
						"**ATENCION!!** Tiempo excedido para ciclo generación de market data en el intervalo. Generado "
								+ quantity);
				break;
			}

			// PUBLISH TO KAFKA
			// emitter.send(fixMessage);
		}

		System.out.println(("generado " + quantity));
		tiempo_restante_loop = interval - (System.currentTimeMillis() - time);
		if (tiempo_restante_loop > 0) {
			esperar(tiempo_restante_loop);
		}

	}

	private void esperar(long tiempo_restante_loop) {
		try {
			Thread.currentThread();
			Thread.sleep(tiempo_restante_loop);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
