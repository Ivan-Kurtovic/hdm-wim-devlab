package rest;

import com.google.gson.Gson;
import controller.CustomKeywordFilter;
import controller.DriveTest;
import controller.ElasticsearchCommunication;
import controller.GoogleDriveCommunication;
import controller.Protocol;
import de.hdm.wim.sharedLib.Constants.PubSub.Config;
import de.hdm.wim.sharedLib.Constants.PubSub.Topic.ST_TOKEN;
import de.hdm.wim.sharedLib.events.IEvent;
import de.hdm.wim.sharedLib.events.TokenEvent;
import de.hdm.wim.sharedLib.helper.Helper;
import de.hdm.wim.sharedLib.pubsub.helper.PublishHelper;
import java.util.ArrayList;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import models.ElasticsearchResult;
import models.KeywordFilter;
import models.TextInformation;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@Path("/rest")
public class Rest {
	final static Logger logger = Logger.getLogger(Rest.class);
	private static String sesseionType = "";
	private static ArrayList<TextInformation> listTextinformations;
	private final Helper helper = new Helper();
	private ElasticsearchCommunication elasticsearchCommunication = new ElasticsearchCommunication();
	private CustomKeywordFilter customKeywordFilter = new CustomKeywordFilter();
	private Protocol protocol = new Protocol();
	private ArrayList<String> listEvent = null;
	

	@GET @Path("test")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response findByName() throws Exception {
		
		DriveTest driveTest = new DriveTest();
		driveTest.ausgabe();
		String protocolName = protocol.createProtocol(listTextinformations);
		
		return Response.status(200).entity("test").build();
	}

	@POST
	@Path(Config.PUSH_ENDPOINT_PREFIX + ST_TOKEN.HANDLER_ID)
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response receivePush(String json) throws Exception {

		/*
		Use this json for testing with SoapUI:
		{
			"message":
				{
					"data":"dGVzdA==",
					"attributes":
					{
						"EventType":"token"
					},
					"messageId":"91010751788941",
					"publishTime":"2017-04-05T23:16:42.302Z"
				}
			}
		 */


		// TODO: change to TokenEvent in production
		IEvent event = helper.convertToIEvent(json);
		System.out.println(event.getEventType());
		if(event.getEventType().equals("SessionStartEvent")){
			sesseionType = "start";
			listTextinformations = new ArrayList<TextInformation>();
			
		} else if(event.getEventType().equals("SessionEndEvent")){
			sesseionType = "end";
			String protocolName = protocol.createProtocol(listTextinformations);		
			GoogleDriveCommunication.saveProtocolOnGoogleDrive(protocolName);
			
		}
		
		// just to display converted event in response
		String eventJson = new Gson().toJson(event);

		// statuscode 200 to ACK messages, so pubsub knows they arrived and it does not have to re-send them
		if(true){
			return Response.status(200).entity(eventJson).build();
		}
		// statuscode 500 to NACK messages, so pubsub know sth. went wrong and the message has to be re-sent
		else{
			return Response.status(500).entity(eventJson).build();
		}
	}

	@POST @Path("token")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response receiveToken(Object objToken) throws Exception {
		if(sesseionType=="start"){			
			ObjectMapper mapper = new ObjectMapper();
			ElasticsearchResult elasticsearch = new ElasticsearchResult();
			ArrayList<String> listKontexte = new ArrayList<String>();

			if(logger.isInfoEnabled()){
				logger.info("Received Information from GUI: " + objToken);
			}

			//parse received json to JAVA-String
			String jsonInString = mapper.writeValueAsString(objToken);
			if(logger.isInfoEnabled()){
				logger.info("Parsed GUI-Information to JSON: " + jsonInString);
			}

			//Die jeweiligen Attribute des erhaltenen JSONS in ein JAVA-Objekt mappen
			TextInformation token = mapper.readValue(jsonInString, TextInformation.class);

			elasticsearch.setUserID(token.getUserID());
			elasticsearch.setSessionID(token.getSessionID());
			elasticsearch.setTimestamp(token.getTimestamp());

			listKontexte.add("Videokonferenz");
			elasticsearch.setListKontexte(listKontexte);

			PublishHelper ph = new PublishHelper(false);

			listTextinformations.add(token);
			try{
				if(ElasticsearchCommunication.checkExistingFilter() == true){
					TokenEvent tokenEvent 	= startTokenaziation(elasticsearch, token);
					String tokenEventString =  new Gson().toJson(tokenEvent);
					
					//ph.Publish(tokenEvent, Topic.TOPIC_1, true);

					return Response.status(200).entity(tokenEventString).build();
				} else {
					//creating new Elasticsearch index with the custom filter
					ElasticsearchCommunication.createCustomFilter();

					TokenEvent tokenEvent 	= startTokenaziation(elasticsearch, token);
					String tokenEventString =  new Gson().toJson(tokenEvent);

					//ph.Publish(tokenEvent, Topic.TOPIC_1, true);
					
				
					return Response.status(200).entity(tokenEventString).build();
				}
			}
			catch ( Exception e	){
				return Response.status(500).entity(e.toString()).build();
			}
			
		} else if(sesseionType == "end"){
			Response.status(500).entity("session is ended").build();
		} 
		return Response.status(500).entity("no session found").build(); 
	}

	public TokenEvent startTokenaziation(ElasticsearchResult elasticsearch, TextInformation token) throws Exception{
		TokenEvent tokenevent = new TokenEvent();

		KeywordFilter keywordfilter = customKeywordFilter.keywordFilter(token.getTextresultat());

		elasticsearch.setListTokens(elasticsearchCommunication.sendToElasticSearch(keywordfilter.getTokenWithoutKeywords()));
		ArrayList<String> listTokens = new ArrayList<String>(elasticsearch.getListTokens());
		listTokens.addAll(keywordfilter.getListFilteredKeywords());

		//creating jsonobject for response
		try {
			tokenevent = createTokenEventForResponse(elasticsearch, listTokens);
		} catch (Exception err) {
			logger.error("Could not parse JSONObject:"+err);
		}
		return tokenevent;
	}


	public JSONObject createJSONObjectForResponse(ElasticsearchResult elasticsearch, ArrayList<String> listTokens) throws JSONException{
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("sessionID", elasticsearch.getSessionID());
		jsonObject.put("userID", elasticsearch.getUserID());
		jsonObject.put("timestamp", elasticsearch.getTimestamp());
		jsonObject.put("kontexts", elasticsearch.getListKontexte());
		jsonObject.put("tokens", listTokens);
		return jsonObject;
	}

	public TokenEvent createTokenEventForResponse(ElasticsearchResult elasticsearch, ArrayList<String> listTokens) throws Exception{

		TokenEvent tokenevent = new TokenEvent();

		tokenevent.setData("test123");
		tokenevent.setSessionId(elasticsearch.getSessionID());
		tokenevent.setUserId(elasticsearch.getUserID());
		//	tokenevent.put("timestamp", elasticsearch.getTimestamp());
		tokenevent.setContexts(elasticsearch.getListKontexte().toString());
		tokenevent.setTokens(listTokens.toString());
		return tokenevent;
	}
}