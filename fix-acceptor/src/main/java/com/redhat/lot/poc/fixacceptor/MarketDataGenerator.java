package com.redhat.lot.poc.fixacceptor;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.InvalidMessage;
import quickfix.Message;
import quickfix.field.TransactTime;

public class MarketDataGenerator implements Runnable {

	private final static Logger log = LoggerFactory.getLogger(MarketDataGenerator.class);
	// private static String msg = "8=FIX.4.49=12835=D34=449=STUN52=20210715-21:06:54.41656=EXEC11=162638321441821=138=340=154=155=VALE59=060=changedate10=015";
	private static final String msg = "8=FIXT.1.160=changedate9=21335=X34=1485349=BYMA_MDP52=20210902-13:30:05.92056=dmx265-1115=FGW262=HUB562_MDP_16305804053851021=2268=1279=0269=055=PAMP48=PAMP-0003-C-CT-ARS167=CS207=XMEV106=0103-P270=119271=2346=1290=463=310=145";

	private String fixDatePattern = "YYYYMMdd-HH:mm:ss.SSS";
	private static SimpleDateFormat simpleDateFormat;
	private boolean play = true;
	private int quantity = 100;

	private long duration;
	private long initPerSecondTime;
	private long currenttime;
	private long totalMessagesGenerated;

	private int errors = 0;
	private int time_left=0;
	private String msg2;
	private int i;
	private boolean isKafka = false;

	private KafkaProducer<String, String> producer;
	private Metrics metrics;

	private static MarketDataGenerator instance;

	public MarketDataGenerator(int quantity, int duration, boolean isKafka, Metrics metrics) {
		this.quantity = quantity;
		this.duration = duration;
		this.isKafka = isKafka;
		this.metrics = metrics;
  	}
	
	public static MarketDataGenerator getInstance() {
		if(instance == null)
			instance = new MarketDataGenerator();
		return instance;
	}
	
	public MarketDataGenerator() {}

	@Override
	public void run() {
		
		totalMessagesGenerated = 0;
		
		// End Time = Time until the thread will be executed
		endTime = System.currentTimeMillis() + duration;
		currenttime = System.currentTimeMillis();
		
		log.info(">>> Arrancando... Generando " + quantity+ "] mensajes  Durante "+duration+" ms, NanoNow: "+currenttime+", endTime: "+endTime);
		
		simpleDateFormat = new SimpleDateFormat(fixDatePattern);
		
		try {
			while (play) {
				generateMarketData2();
			}
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}

	}

	public void generateMarketData2() throws InterruptedException {
		
		msg2 = MarketDataGenerator.generateStringMessage(); //all with the same timestamp in each cycle
		i = 0;
		initPerSecondTime = System.nanoTime();

		for (i = 0; i < (quantity); i++) {
			
			if (isKafka) {
				producer.send(new ProducerRecord<>("marketdata", msg2));
			} else {
				CircularList.getInstance().insert(msg2);
			}
		}
		
		totalMessagesGenerated = totalMessagesGenerated + i;
		System.out.printf("> Messages sent to Kafka: %d\r", totalMessagesGenerated);
		time_left = 1000000 - (int) (System.nanoTime() - initPerSecondTime) ;
		if (time_left < 0) {
			//means that took more than 1 milisecond
			errors++;
		} else {
			Thread.currentThread().sleep(0,time_left);
		}
		
	}


	public static Message generateMessage(){
		Message fixMessage = new Message();

		try {
			fixMessage.fromString(msg, null, false);
			//fixMessage.setField(new DoubleField(60, System.currentTimeMillis() ));
			
			fixMessage.setField(new TransactTime( java.time.LocalDateTime.now() ));
			
		} catch (InvalidMessage e) {
			e.printStackTrace();
		}

		return fixMessage;
	}
	
	public static String generateStringMessage(){
		String strFixMessage = msg;//"8=FIX.4.49=12835=D34=449=STUN52=20210901-00:06:54.41656=EXEC11=162638321441821=138=340=154=155=VALE59=0";

		try {
			strFixMessage = strFixMessage.replace("changedate", simpleDateFormat.format(new Date()));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return strFixMessage;
	}

	private void esperar(long tiempo_restante_loop) {
		int nanos = 0;
		long tiempo_restante_loop_milis = 0;
		try {
			if (tiempo_restante_loop > 999999) {
				tiempo_restante_loop_milis = tiempo_restante_loop /1000000;
				
			}
			nanos = (int) tiempo_restante_loop % 1000000;
			
			Thread.currentThread().sleep(tiempo_restante_loop_milis, nanos);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		
		log.info((">>> Time is up ("+duration+" ms)! Stopping thread..."));
		log.info((">>> Total Messages Generated... ("+totalMessagesGenerated+")"));
		
		metrics.logMetrics();
		
		play = false;
	}

	//end time execution, since initTime (inittime + (duration in milliseconds))
	private long endTime;

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public void setPlay(boolean play) {
		this.play = play;
	}
	
	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public void setKafkaProducer(KafkaProducer<String, String> p){
		this.producer = p;
	}
}
