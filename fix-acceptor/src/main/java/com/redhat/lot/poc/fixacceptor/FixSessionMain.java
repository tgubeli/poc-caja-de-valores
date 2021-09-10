// package com.redhat.lot.poc.fixacceptor;

// import java.io.FileNotFoundException;
// import java.io.InputStream;

// import io.quarkus.runtime.Quarkus;
// import io.quarkus.runtime.annotations.QuarkusMain;
// import quickfix.SessionSettings;

// @QuarkusMain
// public class FixSessionMain {

// 	public static void main(String... args) throws Exception {
// 		try (InputStream inputStream = getSettingsInputStream(args)) {

// 			SessionSettings settings = new SessionSettings(inputStream);
// 			inputStream.close();

// 			Executor executor = new Executor(settings);
// 			executor.start();

// 			Quarkus.run(args); 
//             Quarkus.waitForExit();

// 			executor.stop();
// 		}
// 	}

// 	private static InputStream getSettingsInputStream(String[] args) throws FileNotFoundException {
// 		InputStream inputStream = Executor.class.getResourceAsStream("executor.cfg");
// 		if (inputStream == null) {
// 			System.out.println("usage: " + Executor.class.getName() + " [configFile].");
// 			System.exit(1);
// 		} else {
// 			System.out.println("Arquivo de conf de FIX encontrado!");
// 		}
// 		return inputStream;
// 	}
// }