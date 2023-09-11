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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ScimService {

    private static Logger LOG = LoggerFactory.getLogger(ScimService.class);
    private static JansUtil jansUtil = new JansUtil();
    
    private String getScimUserSearchEndpoint() {
        String scimUserSearchEndpoint = jansUtil.getScimUserSearchEndpoint();
        LOG.info(" scimUserSearchEndpoint:{}", scimUserSearchEndpoint);
        return scimUserSearchEndpoint;
    }
    
    private String requestAccessToken() {
        //String token = jansUtil.requestScimAccessToken();
        String token = Constants.AUTH_TOKEN;
        LOG.info(" token:{}", token);
        return token;
    }

    public UserResource getUserById(String inum) {
        LOG.info(" inum:{}", inum);
        System.out.println("inum = "+inum);
        try {
            System.out.println("ScimService()::getUserById() - inum = "+inum);
            String filter = "id eq \""+inum+"\"";
            return makePostRequest(getScimUserSearchEndpoint() +"/"+ inum,this.requestAccessToken(),filter);
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
            System.out.println("ScimService()::getUserByName() - username = "+username);
            String filter = "userName eq \""+username+"\"";
            return postData(this.getScimUserSearchEndpoint() ,this.requestAccessToken(),filter);
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
            System.out.println("ScimService()::getUserById() - email = "+email);
            String filter = "emails[value eq \"" + email + "\"]";
            return makePostRequest(this.getScimUserSearchEndpoint(),this.requestAccessToken(),filter);
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error("Error fetching user based on email:{} from external service is:{} - {} ", email, ex.getMessage(), ex);
            System.out.println("Error fetching user based on email="+email+" from external service is->"+ex);
        }
        return null;
    }
    
    private UserResource makePostRequest(String uri, String accessToken, String filter) throws IOException {
        LOG.info(" makePostRequest() - uri:{}, accessToken:{}", uri, accessToken);
        System.out.println("makePostRequest() - uri = "+uri+" , accessToken ="+accessToken);
        SearchRequest searchRequest = createSearchRequest(filter);
        
        Builder clientRequest = jansUtil.getClientBuilder(uri);    
        clientRequest.header("Authorization", "Bearer " + accessToken);
        LOG.info(" makePostRequest() - clientRequest:{}", clientRequest);
        
        Invocation invocation = clientRequest.buildPost(Entity.entity(searchRequest, MediaType.APPLICATION_JSON));
        
        System.out.println("makePostRequest() - invocation = "+invocation+" \n\n");

        Response response = invocation.invoke();
        LOG.info(" makePostRequest() - response:{}", response);
        System.out.println("makePostRequest() - response = "+response+" \n\n");
        
        UserResource user = null;
        if(response!=null) {
            LOG.info(" makePostRequest() - response.getEntity():{}, response.getClass():{}", response.getEntity(),response.getClass());
            System.out.println("makePostRequest() - response.getEntity() = "+response.getEntity()+", response.getClass() = "+response.getClass()+" \n\n");
        }
        
        //TO-Test - start
        postData(uri,accessToken,filter);
      //TO-Test - end
        
        return user;
    }
    
    public UserResource postData(String uri, String accessToken, String filter) {
        UserResource user = null;
        try{
            HttpClient client = HttpClientBuilder.create().build();
        
        
        SearchRequest searchRequest = createSearchRequest(filter);
        LOG.info(" postData() - client:{}, searchRequest:{}", client, searchRequest);
        System.out.println("postData() - client = "+client+", searchRequest = "+searchRequest+" \n\n");
        
        //user = SimpleHttp.doPost(uri, client).auth(accessToken).json(searchRequest).asJson(UserResource.class);
        ListResponse listResponse = SimpleHttp.doPost(uri, client).auth(accessToken).json(searchRequest).asJson(ListResponse.class);
                LOG.info(" postData() - listResponse:{}", listResponse);
        System.out.println("postData() - listResponse = "+listResponse+"\n\n");    
        if(listResponse!=null && listResponse.getResources()!=null && listResponse.getResources().size()>0) {
        user=listResponse.getResources().stream().map(UserResource.class::cast).findFirst().get();
        }
        LOG.info(" postData() - user:{}", user);
        System.out.println("postData() - user = "+user+"\n\n");
        }catch(Exception ex){
            ex.printStackTrace();
            LOG.error("\n\n Error while fetching data is ex:{}",ex);
        }
        return user;
    } 
    
    private SearchRequest createSearchRequest(String filter) {
        LOG.info(" createSearchRequest() - filter:{}", filter);
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setFilter(filter);
        
        LOG.info(" createSearchRequest() - searchRequest:{}", searchRequest);
        System.out.println("ScimService()::createSearchRequest() - searchRequest = "+searchRequest+" \n\n");
        return searchRequest;
    }
        

}
