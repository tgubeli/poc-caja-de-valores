package com.redhat.lot.poc;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

public class Metrics {

	private static Metrics instance;
	
	private Map<String, double[]> metricsPerSession = new HashMap<String, double[]>(130);
	
	private long cant_messages=0;
	
	/**
	 * Metrics per millisecond ranges, there are 15 ranges with a counter for each one:
	 * {0-1, 2-3, 4-5, 6-7, 8-9, 10-15, 16-30, 31-90, 91-150, 151-200, 201-300, 301-400, 401-600, 601-1000, 1001-infinite}
	 * Totalized metrics will be group here
	 */
	private int[] metricsPerRange = new int[15];
	private static int[][] metricsRanges = new int[15][2];
	
	
	public static Metrics getInstance() {
		if(instance==null) {
			instance = new Metrics();
			setRanges();
		}
		return instance;
	}
	
	
	
	/**
	 * 
	 * @param sessionID
	 * @param initMilis
	 * @param endMilis
	 */
	public void addMetric(String sessionID, LocalDateTime initDateTime,  LocalDateTime endDateTime) {
	//public void addMetric(String sessionID, double initMilis,  long endMilis) {
		
		long initMilis = initDateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
		long endMilis = endDateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
		
		double diferenciaTimestamp = endMilis - initMilis;
		
		// System.out.println("Milis: Init: "+initMilis+", End: "+endMilis+", Dif: "+diferenciaTimestamp);
		
		if(!metricsPerSession.containsKey(sessionID)) {
			double[] valores = {diferenciaTimestamp,diferenciaTimestamp,diferenciaTimestamp,1d}; // {min,max,sumaTimestamp, cantidadMensajes}
			metricsPerSession.put(sessionID, valores);
			
		}else {
			
			double[] valoresActuales = metricsPerSession.get(sessionID);
			
			if( valoresActuales[0] > diferenciaTimestamp)
				valoresActuales[0] = diferenciaTimestamp;
			if( valoresActuales[1] < diferenciaTimestamp)
				valoresActuales[1] = diferenciaTimestamp;
			
			valoresActuales[2] = valoresActuales[2] + diferenciaTimestamp;
			valoresActuales[3] = valoresActuales[3] + 1d;
				
			metricsPerSession.put(sessionID, valoresActuales);
		}
		
		addRangeMetricCount((int) diferenciaTimestamp);
		cant_messages++;
			
	}
	
	private void addRangeMetricCount(int duracion) {
		// totalized metrics
		for(int i =0; i < metricsRanges.length; i++) {
			if(duracion>=metricsRanges[i][0] && duracion<=metricsRanges[i][1]) {
				metricsPerRange[i] = metricsPerRange[i]>=0? metricsPerRange[i]+1 : 1;
				break;
			}
		}
	}
	
	public void logMetrics() {
		
		
		// copy all metrics in a new map
		Map<String, double[]> hashMetricsTemp = new HashMap<String, double[]>(130);
		hashMetricsTemp = metricsPerSession;
		int[] metricsPerRangeTemp = metricsPerRange;
		
		// delete/reset the main matrics Map (because meanwhile we print the actual metrics, 
		// other new metrics could appear and should be added/saved)
		metricsPerSession = new HashMap<String, double[]>(130);
		metricsPerRange = new int[15];
		
		Object[] sessionIds = (Object[]) hashMetricsTemp.keySet().toArray();
		double media = 0;
		double maxFinal = 0;
		double minFinal = 0;
		double mediaFinal = 0;
		
		// print/log all metrics per Session ID and collect/calculate a Total Metrics values
		int i = 0;
		for(double[] valoresActuales : hashMetricsTemp.values()) {
			media = valoresActuales[2] / valoresActuales[3];
			System.out.println(">>> Metrics Resume for SessionID ["+(String)sessionIds[i]+"]: max["+valoresActuales[1]+"], min["+valoresActuales[0]+"], med["+media+"]");
			
			if( valoresActuales[0] < minFinal)
				minFinal = valoresActuales[0];
			if( valoresActuales[1] > maxFinal)
				maxFinal = valoresActuales[1];
			
			mediaFinal = mediaFinal + media;
			
			i=i+1;
		}
		
		System.out.println(">>>>> TOTAL METRICS: max["+maxFinal+"], min["+minFinal+"], med["+(mediaFinal/i)+"], cant sessions " + sessionIds.length + ", cant_messages=" + cant_messages);
		
		// Metrics by time range
		System.out.println("\t >>>>> METRICS BY TIME RANGE:");
		for(int j =0; j < metricsRanges.length; j++) {
			System.out.println("\t\t Entre: "+metricsRanges[j][0]+"ms y "+metricsRanges[j][1]+"ms = "+metricsPerRangeTemp[j]);
		}
		
		cant_messages=0;
		
	}
	
	/**
	 * Metrics per millisecond ranges, there are 15 ranges with a counter for each one:
	 * {0-1, 2-3, 4-5, 6-7, 8-9, 10-15, 16-30, 31-90, 91-150, 151-200, 201-300, 301-400, 401-600, 601-1000, 1001-infinite}
	 * Totalized metrics will be group here
	 */
	private static void setRanges() {
		metricsRanges[0][0] = 0;
		metricsRanges[0][1] = 1;
		metricsRanges[1][0] = 2;
		metricsRanges[1][1] = 3;
		metricsRanges[2][0] = 4;
		metricsRanges[2][1] = 5;
		metricsRanges[3][0] = 6;
		metricsRanges[3][1] = 7;
		metricsRanges[4][0] = 8;
		metricsRanges[4][1] = 9;
		metricsRanges[5][0] = 10;
		metricsRanges[5][1] = 15;
		metricsRanges[6][0] = 16;
		metricsRanges[6][1] = 30;
		metricsRanges[7][0] = 31;
		metricsRanges[7][1] = 90;
		metricsRanges[8][0] = 91;
		metricsRanges[8][1] = 150;
		metricsRanges[9][0] = 151;
		metricsRanges[9][1] = 200;
		metricsRanges[10][0] = 201;
		metricsRanges[10][1] = 300;
		metricsRanges[11][0] = 301;
		metricsRanges[11][1] = 400;
		metricsRanges[12][0] = 401;
		metricsRanges[12][1] = 600;
		metricsRanges[13][0] = 601;
		metricsRanges[13][1] = 1000;
		metricsRanges[14][0] = 1001;
		metricsRanges[14][1] = 999999999;
		
	}



	public void remove(String sessionID) {
		metricsPerSession.remove(sessionID);
		// TODO Auto-generated method stub
		
	}
	
}
