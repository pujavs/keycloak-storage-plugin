package io.jans.idp.keycloak.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.util.ClassUtil;

import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import io.jans.as.common.util.AttributeConstants;
import io.jans.scim.model.scim2.SearchRequest;
import io.jans.scim.model.scim2.user.UserResource;
import io.jans.scim2.client.rest.ClientSideService;
import io.jans.scim.model.scim2.ListResponse;
import io.jans.orm.model.PagedResult;

import io.jans.idp.keycloak.util.Constants;
import io.jans.idp.keycloak.util.JansUtil;

import java.io.IOException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.WebApplicationException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.keycloak.util.JsonSerialization;

public class ScimService {

    private static Logger LOG = LoggerFactory.getLogger(ScimService.class);
    private static JansUtil jansUtil = new JansUtil();

    private String getScimUserSearchEndpoint() {
        String scimUserSearchEndpoint = jansUtil.getScimUserSearchEndpoint();
        LOG.info(" scimUserSearchEndpoint:{}", scimUserSearchEndpoint);
        return scimUserSearchEndpoint;
    }

    private String requestAccessToken() {
        String token = null;
  
        try {
        token = jansUtil.requestScimAccessToken();
        //String token = Constants.AUTH_TOKEN;
        LOG.info(" token:{}", token);
        }catch(Exception ex) {
            LOG.error(" Error while generating access token for SCIM endpoint is:{}", ex);
            throw new WebApplicationException("Error while generating access token for SCIM endpoint is = "+ex);
        }
        return token;
    }

    public UserResource getUserById(String inum) {
        LOG.info(" inum:{}", inum);
        System.out.println("inum = " + inum);
        try {
            System.out.println("ScimService()::getUserById() - inum = " + inum);
            String filter = "id eq \"" + inum + "\"";
            return postData(getScimUserSearchEndpoint() + "/" + inum, this.requestAccessToken(), filter);
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error("Error fetching user based on inum:{} from external service is:{} - {} ", inum, ex.getMessage(),
                    ex);
            System.out.println("Error fetching user based on inum=" + inum + " from external service is->" + ex);
        }
        return null;
    }

    public UserResource getUserByName(String username) {
        LOG.info(" username:{}", username);
        try {
            System.out.println("ScimService()::getUserByName() - username = " + username);
            String filter = "userName eq \"" + username + "\"";
            return postData(this.getScimUserSearchEndpoint(), this.requestAccessToken(), filter);
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error("Error fetching user based on username:{} from external service is:{} - {} ", username,
                    ex.getMessage(), ex);
            System.out
                    .println("Error fetching user based on username=" + username + " from external service is->" + ex);
        }
        return null;
    }

    public UserResource getUserByEmail(String email) {
        LOG.info(" email:{}", email);
        try {
            System.out.println("ScimService()::getUserById() - email = " + email);
            String filter = "emails[value eq \"" + email + "\"]";
            return postData(this.getScimUserSearchEndpoint(), this.requestAccessToken(), filter);
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error("Error fetching user based on email:{} from external service is:{} - {} ", email, ex.getMessage(),
                    ex);
            System.out.println("Error fetching user based on email=" + email + " from external service is->" + ex);
        }
        return null;
    }

   

    public UserResource postData(String uri, String accessToken, String filter) {
        UserResource user = null;
        try {
            HttpClient client = HttpClientBuilder.create().build();

            SearchRequest searchRequest = createSearchRequest(filter);
            LOG.info(" postData() - client:{}, searchRequest:{}", client, searchRequest);
            System.out.println("postData() - client = " + client + ", searchRequest = " + searchRequest + " \n\n");

            JsonNode jsonNode = SimpleHttp.doPost(uri, client).auth(accessToken).json(searchRequest).asJson();
            LOG.info("\n\n new  postData() - jsonNode:{}", jsonNode);
            System.out.println("postData() - jsonNode = "+jsonNode+"\n\n");
          
            LOG.info(" postData() - user:{}", user);
            System.out.println("postData() - user = " + user + "\n\n");
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error("\n\n Error while fetching data is ex:{}", ex);
        }
        return user;
    }

    private SearchRequest createSearchRequest(String filter) {
        LOG.info(" createSearchRequest() - filter:{}", filter);
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setFilter(filter);

        LOG.info(" createSearchRequest() - searchRequest:{}", searchRequest);
        System.out.println("ScimService()::createSearchRequest() - searchRequest = " + searchRequest + " \n\n");
        return searchRequest;
    }

    private UserResource getUserResource(JsonNode jsonNode) {
        LOG.info(" \n\n getUserResource() - jsonNode:{}", jsonNode);
        System.out.println("getUserResource() - jsonNode = " + jsonNode + "\n\n");
        UserResource user = null;
        try {
            
            if (jsonNode != null) {
                
               
                
                if(jsonNode.get("Resources")!=null) {
                    JsonNode value = jsonNode.get("Resources").get(0);
                    LOG.info("\n\n *** getUserResource() - value:{}, value.getClass():{}", value, value.getClass());
                    user = JsonSerialization.readValue(JsonSerialization.writeValueAsBytes(value), UserResource.class);
                    LOG.info(" getUserResource() - user:{}, user.getClass():{}", user, user.getClass());
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error("\n\n Error while fetching data is ex:{}", ex);
        }
        return user;
    }
}
