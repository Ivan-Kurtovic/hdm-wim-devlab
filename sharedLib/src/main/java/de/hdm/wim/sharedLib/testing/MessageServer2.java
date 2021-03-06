package de.hdm.wim.sharedLib.testing;

import com.google.gson.Gson;
import de.hdm.wim.sharedLib.Constants;
import de.hdm.wim.sharedLib.events.DocumentInformationEvent;
import de.hdm.wim.sharedLib.events.StayAliveEvent;
import de.hdm.wim.sharedLib.events.SuccessfulFeedbackEvent;
import de.hdm.wim.sharedLib.events.UserInformationEvent;
import de.hdm.wim.sharedLib.events.UserJoinedSessionEvent;
import de.hdm.wim.sharedLib.events.UserLeftSessionEvent;
import de.hdm.wim.sharedLib.pubsub.classes.PubSubMessage;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 * The type Message server 2.
 *
 * @author Nils Bachmann
 * @createdOn 24.06.2017
 */
public class MessageServer2 {

	private static final Executor SERVER_EXECUTOR 	 = Executors.newSingleThreadExecutor();
	private static final int PORT 					 = 9999;
	private static final long MESSAGE_PERIOD_SECONDS = 6;
	private static final Logger LOGGER 				 = Logger.getLogger(MessageServer2.class);

	/**
	 * The entry point of this application. It will send one message every 10 seconds until terminated
	 * or the queue is full
	 *
	 * use "telnet localhost port" in cmd to see the messages
	 *
	 * @param args the input arguments
	 * @throws IOException the io exception
	 * @throws InterruptedException the interrupted exception
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		BlockingQueue<String> messageQueue  = new ArrayBlockingQueue<>(100);
		int id 								= 0;

		SERVER_EXECUTOR.execute(new MessageServer2.SteamingServer(messageQueue));

		//Anlegen beispielhafter Events

		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());

		DocumentInformationEvent docinfevt = new DocumentInformationEvent();
		docinfevt.setId("1");
		docinfevt.setData("bla");
		docinfevt.setPublishTime(timeStamp);
		docinfevt.setEventSource(Constants.PubSub.EventSource.SEMANTIC_REPRESENTATION);
		docinfevt.setDocumentId("123");
		docinfevt.setDocumentBelongsToProject("HighNet");

		DocumentInformationEvent docinfevt2 = new DocumentInformationEvent();
		docinfevt2.setId("2");
		docinfevt2.setData("bla");
		docinfevt2.setEventSource(Constants.PubSub.EventSource.SEMANTIC_REPRESENTATION);
		docinfevt2.setPublishTime(timeStamp);
		docinfevt2.setDocumentId("233");
		docinfevt2.setDocumentBelongsToProject("HighNet");

		DocumentInformationEvent docinfevt3 = new DocumentInformationEvent();
		docinfevt3.setId("3");
		docinfevt3.setData("bla");
		docinfevt3.setEventSource(Constants.PubSub.EventSource.SEMANTIC_REPRESENTATION);
		docinfevt3.setPublishTime(timeStamp);
		docinfevt3.setDocumentId("124");
		docinfevt3.setDocumentBelongsToProject("HighNet");


		StayAliveEvent stayAlive = new StayAliveEvent();
		stayAlive.setPublishTime(timeStamp);
		stayAlive.setUserId("Bachmann");

		StayAliveEvent stayAlive2 = new StayAliveEvent();
		stayAlive2.setPublishTime(timeStamp);
		stayAlive2.setUserId("Schneider");

		StayAliveEvent stayAlive3 = new StayAliveEvent();
		stayAlive2.setPublishTime(timeStamp);
		stayAlive2.setUserId("Krasniqi");

		UserJoinedSessionEvent joinedSessionEvent = new UserJoinedSessionEvent();
		joinedSessionEvent.setSessionId("12345");
		joinedSessionEvent.setPublishTime(timeStamp);
		joinedSessionEvent.setUserId("Bachmann");

		UserJoinedSessionEvent joinedSessionEvent2 = new UserJoinedSessionEvent();
		joinedSessionEvent2.setSessionId("12345");
		joinedSessionEvent2.setPublishTime(timeStamp);
		joinedSessionEvent2.setUserId("Schneider");

		UserJoinedSessionEvent joinedSessionEvent3 = new UserJoinedSessionEvent();
		joinedSessionEvent3.setSessionId("12345");
		joinedSessionEvent3.setPublishTime(timeStamp);
		joinedSessionEvent3.setUserId("Krasniqi");

		UserLeftSessionEvent leftSessionEvent = new UserLeftSessionEvent();
		leftSessionEvent.setSessionId("12345");
		leftSessionEvent.setUserId("Bachmann");

		UserLeftSessionEvent leftSessionEvent2 = new UserLeftSessionEvent();
		leftSessionEvent2.setSessionId("12345");
		leftSessionEvent2.setUserId("Schneider");

		UserLeftSessionEvent leftSessionEvent3 = new UserLeftSessionEvent();
		leftSessionEvent3.setSessionId("12345");
		leftSessionEvent3.setUserId("Krasniqi");

		UserInformationEvent userInformationEvent1 = new UserInformationEvent();
		userInformationEvent1.setLastname("Bachmann");
		userInformationEvent1.setUserWorksOnProject("HighNet");
		userInformationEvent1.setEventSource(Constants.PubSub.EventSource.SEMANTIC_REPRESENTATION);


		UserInformationEvent userInformationEvent2 = new UserInformationEvent();
		userInformationEvent2.setLastname("Krasniqi");
		userInformationEvent2.setUserWorksOnProject("HighNet");
		userInformationEvent2.setEventSource(Constants.PubSub.EventSource.SEMANTIC_REPRESENTATION);


		UserInformationEvent userInformationEvent3 = new UserInformationEvent();
		userInformationEvent3.setLastname("Schneider");
		userInformationEvent3.setUserWorksOnProject("HighNet");
		userInformationEvent3.setEventSource(Constants.PubSub.EventSource.SEMANTIC_REPRESENTATION);


		PubSubMessage message   = PubSubMessage.getRandom("blubb_" + id,Integer.toString(id));
		//String message 			= "test";
		Gson gson               = new Gson();

		//messageQueue.put(gson.toJson(docinfevt));
		// Thread.sleep(TimeUnit.SECONDS.toMillis(MESSAGE_PERIOD_SECONDS));

		SuccessfulFeedbackEvent successfulFeedbackEvent = new SuccessfulFeedbackEvent();
		successfulFeedbackEvent.setUserId("user1");
		successfulFeedbackEvent.setDocumentId("doc1");

		SuccessfulFeedbackEvent successfulFeedbackEvent1 = new SuccessfulFeedbackEvent();
		successfulFeedbackEvent1.setUserId("user1");
		successfulFeedbackEvent1.setDocumentId("doc1");

		// Generieren von Messages in die Queue
		while(true) {

			Thread.sleep(TimeUnit.SECONDS.toMillis(MESSAGE_PERIOD_SECONDS));
			Thread.sleep(TimeUnit.SECONDS.toMillis(MESSAGE_PERIOD_SECONDS));
			Thread.sleep(TimeUnit.SECONDS.toMillis(MESSAGE_PERIOD_SECONDS));
			Thread.sleep(TimeUnit.SECONDS.toMillis(MESSAGE_PERIOD_SECONDS));

			messageQueue.put(gson.toJson(joinedSessionEvent));
			messageQueue.put(gson.toJson(userInformationEvent1));

			Thread.sleep(TimeUnit.SECONDS.toMillis(MESSAGE_PERIOD_SECONDS));

			messageQueue.put(gson.toJson(joinedSessionEvent2));
			messageQueue.put(gson.toJson(userInformationEvent2));

			Thread.sleep(TimeUnit.SECONDS.toMillis(MESSAGE_PERIOD_SECONDS));

			messageQueue.put(gson.toJson(joinedSessionEvent3));
			messageQueue.put(gson.toJson(userInformationEvent3));

			Thread.sleep(TimeUnit.SECONDS.toMillis(MESSAGE_PERIOD_SECONDS));

			messageQueue.put(gson.toJson(leftSessionEvent));

			Thread.sleep(TimeUnit.SECONDS.toMillis(MESSAGE_PERIOD_SECONDS));

			messageQueue.put(gson.toJson(leftSessionEvent2));

			Thread.sleep(TimeUnit.SECONDS.toMillis(MESSAGE_PERIOD_SECONDS));
			messageQueue.put(gson.toJson(leftSessionEvent3));


			Thread.sleep(TimeUnit.SECONDS.toMillis(MESSAGE_PERIOD_SECONDS));
			Thread.sleep(TimeUnit.SECONDS.toMillis(MESSAGE_PERIOD_SECONDS));
		}
	}

	private static class SteamingServer implements Runnable {
		private final BlockingQueue<String> messageQueue;

		/**
		 * Instantiates a new Steaming server.
		 *
		 * @param messageQueue the EVENT queue
		 */
		private SteamingServer(BlockingQueue<String> messageQueue) {
			this.messageQueue = messageQueue;
		}

		@Override
		public void run() {

			try (
				ServerSocket serverSocket   = new ServerSocket(PORT);
				Socket clientSocket         = serverSocket.accept();
				PrintWriter pw             = new PrintWriter(clientSocket.getOutputStream(), true)
			)
			{
				while (true) {
					String message = messageQueue.take();

					LOGGER.info(message);
					pw.println(message);
				}
			} catch (IOException|InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
