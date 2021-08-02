package com.redhat.lot.poc.fixacceptor;

import javax.inject.Inject;

import org.jboss.logging.Logger;

import quickfix.DoubleField;
import quickfix.InvalidMessage;
import quickfix.Message;
import quickfix.UtcTimeStampField;
import quickfix.UtcTimestampPrecision;

public class MarketDataGenerator implements Runnable {

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
			Message message = new Message();
			timestamp = System.currentTimeMillis();
			d = timestamp.doubleValue();
			message.setField(new DoubleField(60, d));
			try {
				message.fromString(msg, null, false);
			} catch (InvalidMessage e) {
				e.printStackTrace();
			}
			CircularList.getInstance().insert(message);
			if (System.currentTimeMillis() - time >= interval) {
				System.out.println(
						"**ATENCION!!** Tiempo excedido para ciclo generación de market data en el intervalo. Generado "
								+ quantity);
				break;
			}

		}

		System.out.println(("generado " + quantity));
		tiempo_restante_loop = interval - (System.currentTimeMillis() - time);
		if (tiempo_restante_loop > 0) {
			esperar(tiempo_restante_loop);
		}

	}

	private void esperar(long tiempo_restante_loop) {
		try {
			Thread.currentThread().sleep(tiempo_restante_loop);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
