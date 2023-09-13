package io.jans.idp.keycloak.provider;

import java.io.IOException;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


import jakarta.ws.rs.WebApplicationException;

import org.keycloak.Config;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JansEventListenerProvider implements EventListenerProvider   {

    private static Logger LOG = LoggerFactory.getLogger(JansEventListenerProvider.class);
    
	private String serverUrl;
    private String username;
    private String password;
	
	public JansEventListenerProvider(String serverUrl, String username, String password) {
        this.serverUrl = serverUrl;
        this.username = username;
        this.password = password;
    }

       @Override
    public void onEvent(Event event) {
           LOG.debug("Event Occurred:" + toString(event));
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {
        LOG.debug("Admin Event Occurred:" + toString(adminEvent));
    }

    @Override
    public void close() {}
    
    private String toString(Event event) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "";
        try {
            jsonString = mapper.writeValueAsString(event);
            //sendJson(jsonString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonString;
    }
    
    private String toString(AdminEvent adminEvent) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "";
        try {
            // An AdminEvent has weird JSON representation field which we need to special case.
            JsonNode representationNode = mapper.readTree(adminEvent.getRepresentation());
            ObjectNode node = mapper.valueToTree(adminEvent);
            node.replace("representation", representationNode);
            jsonString = mapper.writeValueAsString(node);

            //sendJson(jsonString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonString;
    }
}