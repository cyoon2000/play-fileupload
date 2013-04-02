package actors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.String;
import java.lang.System;
import java.lang.Thread;
import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

import models.S3File;
import dto.CountResult;

public class FileReadActor extends UntypedActor {

    public static String DOWNLOAD_LOCATION = "/tmp/";

    private ActorRef masterActor = getContext().actorOf(
            new Props(MasterActor.class), "master");

	@Override
	public void onReceive(Object message) throws Exception {

		if (message instanceof String) {
			String fileId = (String) message;
            S3File s3File = S3File.findById(fileId);
            String fileName = s3File.getActualFileName();
			try {
                File file = new File(DOWNLOAD_LOCATION + s3File.getActualFileName());
                if (file.exists())
                {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
				    String line = null;
                    int count = 0;
				    while ((line = reader.readLine()) != null && count < 20) {
//					    System.out.println("File contents->" + line);
                        masterActor.tell(line);
                        count++;
				    }
                    // tell masterActor to report the result to me
                    Thread.sleep(1000);
                    masterActor.tell(new CountResult(fileId));

                } else {
                    System.out.println("This file " + fileName + " does not exist!");
                }
			} catch (IOException x) {
				System.err.format("IOException: %s%n", x);
			}
        } else if (message instanceof CountResult) {
            // got the result. persist the tag result
            CountResult countResult = (CountResult)message;
            System.out.println("Tag suggestion for the file ID = " + countResult.getFileId() + " : " + countResult.getFinalResultMap());
            S3File s3File = S3File.findById(countResult.getFileId());
            s3File.updateTags(countResult.getFinalResultMap());

		} else
			throw new IllegalArgumentException("Unknown message [" + message
					+ "]");
	}
}