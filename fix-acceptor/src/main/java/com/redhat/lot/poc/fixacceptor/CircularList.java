package com.redhat.lot.poc.fixacceptor;

import java.util.ArrayList;
import quickfix.Message;

public class CircularList {
	private Message[] list;
	

	public static final int MAX = 250000;
	int index = 0;
	private static CircularList singleton;
	private double currentLoop = 0;

	public int getIndex() {
		return index;
	}

	public CircularList() {
		super();
		list = new Message[MAX];
		
		
	}
	
	public void insert (Message msg) {

		list[index] = msg;
		index++;	
		
		if (index==MAX) {
			System.out.println("reseteando index");
			index = 0;
			if (currentLoop < Double.MAX_VALUE) {
				currentLoop++;
			} else {
				currentLoop = 0;
				System.out.println("RESETEANDO CURRENTLOOP");
			}
			
		}
				
							
	}
	
	public Message get (int i) {
		return list[i];
	}
	
	
	public static CircularList getInstance() {
		if (singleton == null ) {
			singleton = new CircularList();
		}
		return singleton;
	}

	public double getCurrentLoop() {
		return currentLoop;
	}
	

}
