package de.hdm.wim.sharedLib.testing;

import com.google.cloud.pubsub.spi.v1.MessageReceiver;
import com.google.pubsub.v1.Subscription;
import de.hdm.wim.sharedLib.Constants.PubSub.Config;
import de.hdm.wim.sharedLib.Constants.PubSub.Topic.TOPIC_1;
import de.hdm.wim.sharedLib.pubsub.helper.SubscriptionHelper;
import de.hdm.wim.sharedLib.pubsub.receiver.ExampleReceiver;

/**
 * TestPush
 * this class is only for testing purposes!
 *
 * @author Benedikt Benz
 * @createdOn 11.06.2017
 * @deprecated
 */
public class TestPush {

	public static void main(String[] args) throws Exception {

		MessageReceiver receiver = new ExampleReceiver();

		// create the subscription
		SubscriptionHelper sh 		= new SubscriptionHelper(false, Config.APP_ID);
		Subscription subscription = sh.CreatePushSubscription(TOPIC_1.TOPIC_ID, TOPIC_1.HANDLER_ID);

		sh.Subscribe(subscription, receiver);
	}
}
