package de.hdm.wim;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.hdm.wim.sharedLib.Constants;
import java.io.IOException;
import java.util.Base64;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by ben on 4/06/2017.
 */
// [START pubsub_appengine_flex_push]
@WebServlet(name = "Push with PubSub" ,value = "/pubsub/push")
public class PubSubPush extends HttpServlet {

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
		throws IOException, ServletException {
		String pubsubVerificationToken = Constants.Config.SECRET_TOKEN;

		// Do not process message if request token does not match pubsubVerificationToken
		if (req.getParameter("token").compareTo(pubsubVerificationToken) != 0) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		// parse message object from "message" field in the request body json
		// decode message data from base64
		Message message = getMessage(req);
		try {
			messageRepository.save(message);
			// 200, 201, 204, 102 status codes are interpreted as success by the Pub/Sub system
			resp.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
// [END pubsub_appengine_flex_push]

	private Message getMessage(HttpServletRequest request) throws IOException {
		String requestBody 		= request.getReader().lines().collect(Collectors.joining("\n"));
		JsonElement jsonRoot 	= jsonParser.parse(requestBody);
		String messageStr 		= jsonRoot.getAsJsonObject().get("message").toString();
		Message message 		= gson.fromJson(messageStr, Message.class);

		// decode from base64
		String decoded = decode(message.getData());
		message.setData(decoded);
		return message;
	}

	private String decode(String data) {
		return new String(Base64.getDecoder().decode(data));
	}

	private final Gson gson = new Gson();
	private final JsonParser jsonParser = new JsonParser();
	private MessageRepository messageRepository;

	PubSubPush(MessageRepository messageRepository) {
		this.messageRepository = messageRepository;
	}

	public PubSubPush() {
		this.messageRepository = MessageRepositoryImpl.getInstance();
	}
}