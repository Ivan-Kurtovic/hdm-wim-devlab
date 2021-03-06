package de.hdm.wim.pubSubWebapp.Helper;

import de.hdm.wim.sharedLib.Constants;
import de.hdm.wim.sharedLib.Constants.RequestParameters;
import de.hdm.wim.sharedLib.events.IEvent;
import de.hdm.wim.sharedLib.helper.Helper;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 * The type Pub sub push handler.
 *
 * @author Benedikt Benz
 * @createdOn 24.06.2017
 */
public class PubSubPushHandler {

	/**
	 * The constant LOGGER.
	 */
	protected static final Logger LOGGER = Logger.getLogger(PubSubPushHandler.class);
	/**
	 * The constant HELPER.
	 */
	protected static Helper HELPER = new Helper();
	/**
	 * The constant EVENTREPOSITORY.
	 */
	protected static EventRepository EVENTREPOSITORY;

	/**
	 * Instantiates a new Pubsubpushhandler.
	 *
	 * @param eventRepository the event repository
	 */
	public PubSubPushHandler(EventRepository eventRepository) {
		EVENTREPOSITORY = eventRepository;
	}

	/**
	 * Instantiates a new Pub sub push handler.
	 */
	public PubSubPushHandler() {
		EVENTREPOSITORY = EventRepositoryImpl.getInstance();
	}

	/**
	 * Process.
	 *
	 * @param req the req
	 * @param resp the resp
	 * @param handler the handler EXAMPLE: {@link de.hdm.wim.sharedLib.Constants.PubSub.Topic.CEP_INSIGHT#HANDLER_ID}
	 * @throws IOException the io exception
	 * @throws ServletException the servlet exception
	 */
	public void process(HttpServletRequest req, HttpServletResponse resp, String handler)
		throws IOException, ServletException {

		String pubsubVerificationToken = Constants.PubSub.Config.SECRET_TOKEN;

		// Do not process message if request token does not match pubsubVerificationToken
		if (req.getParameter(RequestParameters.SECRET_TOKEN).compareTo(pubsubVerificationToken)
			!= 0) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		String requestBody = req.getReader()
			.lines()
			.reduce("\n", (accumulator, actual) -> accumulator + actual);

		LOGGER.info("test123");
		IEvent event = HELPER.convertToIEvent(requestBody);

		try {
			LOGGER.info("Handler: " + handler + " event.getData(): " + event.getData());

			EVENTREPOSITORY.save(event, handler);

			// 200, 201, 204, 102 status codes are interpreted as success by the Pub/Sub system = ACK
			resp.setStatus(HttpServletResponse.SC_OK);

		} catch (Exception e) {
			// NACK
			LOGGER.error(e);
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
