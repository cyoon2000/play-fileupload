package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.String;
import java.lang.System;

import akka.actor.UntypedActor;

public class FileReadActor extends UntypedActor {

	@Override
	public void onReceive(Object message) throws Exception {

		if (message instanceof String) {
//			String fileName = (String) message;

            String fileName = "Othello.txt";

            System.out.println("*** fileName = " + fileName);

			try {
//				BufferedReader reader = new BufferedReader(
//						new InputStreamReader(Thread.currentThread()
//								.getContextClassLoader().getResource(fileName)
//								.openStream()));

//                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
//                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("AwsCredentials.properties");
                InputStreamReader isr = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName));
                BufferedReader reader = new BufferedReader(isr);

				String line = null;
				while ((line = reader.readLine()) != null) {
					System.out.println("File contents->" + line);
					getSender().tell(line);
				}
				System.out.println("All lines send !");
				// send the EOF message..
				getSender().tell(String.valueOf("EOF"));
			} catch (IOException x) {
				System.err.format("IOException: %s%n", x);
			}
		} else
			throw new IllegalArgumentException("Unknown message [" + message
					+ "]");
	}
}