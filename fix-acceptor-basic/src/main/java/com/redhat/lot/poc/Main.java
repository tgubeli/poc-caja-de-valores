package com.redhat.lot.poc;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import quickfix.SessionSettings;

public class Main {
	
	public static void main(String[] args) throws Exception{
		
		try (InputStream inputStream = getSettingsInputStream(args)) {

			SessionSettings settings = new SessionSettings(inputStream);
			inputStream.close();
			
			Executor executor = new Executor(settings);
			executor.start();
			
			
			
			initGenerator(6, 1000, 10000, 6);
			
		}
		
	}
	
	private static InputStream getSettingsInputStream(String[] args) throws FileNotFoundException {
		InputStream inputStream = Main.class.getResourceAsStream("executor.cfg");
		
		if (inputStream == null) {
			System.out.println("usage: " + Executor.class.getName() + " [configFile].");
			System.exit(1);
		} else {
			System.out.println("Arquivo de conf de FIX encontrado!");
		}
		return inputStream;
	}
	
	private static void initGenerator(int quantity, int interval, int duration, int chunks) {
		
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			
			@Override
			public void run() {
				
				MarketDataGenerator generator = MarketDataGenerator.getInstance();
		        generator.setQuantity(quantity);
		        generator.setInterval(interval);
		        generator.setDuration(duration);
		        generator.setChunks(chunks);
		        generator.setPlay(true);
		        
		        Thread t = new Thread(generator);
		        
		        t.start();
				
			}
		};
		timer.schedule(task,10000,10020); // (task, espacio_inicial_tiempo, tick) cada 30 segundos se crean nuevos mensajes

		//timer.cancel();//stop the timer
		
	}
	

}
