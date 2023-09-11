package io.jans.idp.keycloak.service;

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
import io.jans.orm.model.PagedResult;

import io.jans.idp.keycloak.util.JansUtil;

import java.io.IOException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ScimService {

    private static Logger LOG = LoggerFactory.getLogger(UsersApiLegacyService.class);
    private static ClientSideService scimClient = null;
    private static JansUtil jansUtil = new JansUtil();
    
    private String getScimUserEndpoint() {
        String scimUserEndpoint = jansUtil.requestScimAccessToken();
        LOG.info(" scimUserEndpoint:{}", scimUserEndpoint);
        return scimUserEndpoint;
    }
    
    private String requestAccessToken() {
        String token = jansUtil.requestScimAccessToken();
        LOG.info(" token:{}", token);
        return token;
    }

    public UserResource getUserById(String inum) {
        LOG.info(" inum:{}", inum);
        System.out.println("inum = "+inum);
        try {
            System.out.println("UsersApiLegacyService()::getUserById() - inum = "+inum);
            return makeGetRequest(getScimUserEndpoint() +"/"+ inum,this.requestAccessToken());
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error("Error fetching user based on inum:{} from external service is:{} - {} ", inum, ex.getMessage(), ex);
            System.out.println("Error fetching user based on inum="+inum+" from external service is->"+ex);
        }
        return null;
    }
        
    public UserResource getUserByName(String username) {
        LOG.info(" username:{}", username);
        try {
            System.out.println("UsersApiLegacyService()::getUserByName() - username = "+username);
            //return SimpleHttp.doGet(AUTH_USER_ENDPOINT + username, this.session).asJson(User.class);
            return makeGetRequest(this.getScimUserEndpoint() +"?pattern="+username ,this.requestAccessToken());
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error("Error fetching user based on username:{} from external service is:{} - {} ", username, ex.getMessage(), ex);
            System.out.println("Error fetching user based on username="+username+" from external service is->"+ex);
        }
        return null;
    }
    
    public UserResource getUserByEmail(String email) {
        LOG.info(" email:{}", email);
        try {
            System.out.println("UsersApiLegacyService()::getUserById() - email = "+email);
            return makeGetRequest(this.getScimUserEndpoint() +"?pattern="+email ,this.requestAccessToken());
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error("Error fetching user based on email:{} from external service is:{} - {} ", email, ex.getMessage(), ex);
            System.out.println("Error fetching user based on email="+email+" from external service is->"+ex);
        }
        return null;
    }
    
    private UserResource makeGetRequestNew(String uri, String accessToken) throws IOException {
        LOG.info(" makeGetRequest() - uri:{}, accessToken:{}", uri, accessToken);
        System.out.println("UsersApiLegacyService()::makeGetRequest() - uri = "+uri+" , accessToken ="+accessToken);
       
        Builder clientRequest = jansUtil.getClientBuilder(uri);    
        clientRequest.header("Authorization", "Bearer " + accessToken);
        LOG.info(" makeGetRequest() - clientRequest:{}", clientRequest);
        System.out.println("UsersApiLegacyService()::makeGetRequest() - clientRequest = "+clientRequest+" \n\n");

        Response response = clientRequest.get();
        LOG.info(" makeGetRequest() - response:{}", response);
        System.out.println("UsersApiLegacyService()::makeGetRequest() - response = "+response+" \n\n");
        
       
        return null;
    }
    
    
    private UserResource makeGetRequest(String uri, String accessToken) throws IOException {
        LOG.info(" makeGetRequest() - uri:{}, accessToken:{}", uri, accessToken);
        System.out.println("UsersApiLegacyService()::makeGetRequest() - uri = "+uri+" , accessToken ="+accessToken);
       
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(uri);        
        request.addHeader("Authorization", "Bearer " + accessToken);
        request.setHeader("Authorization", "Bearer " + accessToken);
        LOG.info(" makeGetRequest() - client:{}, request:{}, request.getAllHeaders():{}", client, request,request.getAllHeaders());
        System.out.println("UsersApiLegacyService()::makeGetRequest() - client = "+client+" ,request = "+request+" \n\n");

        HttpResponse response =  client.execute(request);
        LOG.info(" makeGetRequest() - response:{}", response);
        System.out.println("UsersApiLegacyService()::makeGetRequest() - response = "+response+" \n\n");
        
       
        UserResource user = SimpleHttp.doGet(this.getScimUserEndpoint(), client).header("Authorization", "Bearer " + accessToken).asJson(UserResource.class);
        LOG.info(" makeGetRequest() - user:{}", user);
        System.out.println("UsersApiLegacyService()::makeGetRequest() - user = "+user+" \n\n");
        
        return user;
    }
        

}
