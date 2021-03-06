package de.hdm.wim.sharedLib.helper;

import com.google.common.io.BaseEncoding;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.pubsub.v1.PubsubMessage;
import de.hdm.wim.sharedLib.events.Event;
import de.hdm.wim.sharedLib.events.IEvent;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.log4j.Logger;

/**
 * Helper class
 *
 * @author Benedikt Benz
 * @createdOn 02.06.2017
 */
public class Helper {

	private static final Logger LOGGER 	= Logger.getLogger(Helper.class);
	private final Gson gson 			= new Gson();
	private final JsonParser jsonParser = new JsonParser();

	/**
	 * Instantiates a new Helper.
	 */
	public Helper() {
	}

	/**
	 * Get random string from list of String.
	 *
	 * @param list the list
	 * @return the string
	 */
	public String getRandomStringFromList(List<String> list) {
		Random random = new Random();
		int index = random.nextInt(list.size());
		return list.get(index);
	}

	/**
	 * Encode base 64 string.
	 *
	 * @param data the data
	 * @return the string
	 * @throws Exception the exception
	 */
	public String encodeBase64(String data) throws Exception {
		byte[] byteString = data.getBytes(data);
		return new String(BaseEncoding.base64().encode(byteString));
	}

	/**
	 * Decode base 64 string.
	 *
	 * @param data the data
	 * @return the string
	 */
	public String decodeBase64(String data) {
		return new String(BaseEncoding.base64().decode(data));
	}

	/**
	 * Convert a PubSubMessage to IEvent
	 *
	 * @param message the PubsubMessage
	 * @return IEvent event
	 */
	public IEvent convertToIEvent(PubsubMessage message) {
		IEvent event = new Event();

		// get message content & transform
		Map<String, String> attributes 	= message.getAttributesMap();
		String data 					= decodeBase64(message.getData().toString());
		String messageId 				= message.getMessageId();
		String publishTime 				= message.getPublishTime().toString();

		// fill new message object
		event.setData(data);
		event.setAttributes(attributes);
		event.setId(messageId);
		event.setPublishTime(publishTime);

		return event;
	}

	/**
	 * Get event from json string.
	 *
	 * @param json the json string of the event
	 * @return IEvent event
	 */
	public IEvent convertToIEvent(String json) {

		JsonElement jsonRoot 	= jsonParser.parse(json);
		JsonElement messageJson = jsonRoot.getAsJsonObject().get("message");
		String messageId = messageJson.getAsJsonObject().get("messageId").getAsString();
		Event event = gson.fromJson(messageJson.toString(), Event.class);

		event.setId(messageId);

		// decode from base64
		String decoded = decodeBase64(event.getData());
		event.setData(decoded);

		return event;
	}

	/**
	 * Convert json to map map.
	 *
	 * @param json the json
	 * @return the map
	 */
	public Map<String, String> convertJsonToMap(String json) {
		Type type = new TypeToken<Map<String, String>>() {
		}.getType();
		Map<String, String> map = gson.fromJson(json, type);

		return map;
	}
}
