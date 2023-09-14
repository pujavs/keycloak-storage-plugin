package io.jans.idp.keycloak.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JacksonUtils;

import io.jans.as.client.TokenClient;
import io.jans.as.client.TokenRequest;
import io.jans.as.client.TokenResponse;
import io.jans.as.model.common.AuthenticationMethod;
import io.jans.as.model.common.GrantType;
import io.jans.as.model.common.ScopeType;
import io.jans.as.model.uma.wrapper.Token;
import io.jans.as.model.util.Util;
import io.jans.idp.keycloak.config.JansConfigSource;
import io.jans.idp.keycloak.client.JansTokenClient;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;


import java.io.IOException;
import java.util.*;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient43Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RegisterProvider(JansTokenClient.class)
public class JansUtil {
    private static Logger LOG = LoggerFactory.getLogger(JansUtil.class);
    JansConfigSource jansConfigSource = new JansConfigSource();
    
    public Map<String, String> getProperties() {
        LOG.error("\n\n *** JansUtil::Getting properties \n\n");
        Map<String, String> props = jansConfigSource.getProperties();
        LOG.error("\n\n JansUtil::getProperties:{}",props);
        
        LOG.error("\n\n JansUtil::getProperties props.get(token.endpoint):{}",props.get("token.endpoint"));
        
        getPropertyNames();
        return props;
    }
    
    public Set<String> getPropertyNames() {
        LOG.error(" \n\n\n ***  JansUtil - getPropertyNames():{}", jansConfigSource.getPropertyNames());
        return jansConfigSource.getPropertyNames();
    }

    
    public String getTokenEndpoint() {
        return Constants.TOKEN_ENDPOINT;
    }
    
    public String getScimUserEndpoint() {
        return Constants.SCIM_USER_ENDPOINT;
    }
    
    public String getScimUserSearchEndpoint() {
        return Constants.SCIM_USER_SEARCH_ENDPOINT;
    }
    
    public String getClientDecryptPassword() {
        return Constants.PASSWORD;
    }
    
    public String getClientId() {
        return Constants.CLIENT_ID;
    }
    
    public static Builder getClientBuilder(String url) {
        return ClientBuilder.newClient().target(url).request();
    }
    
    public String requestScimAccessToken() throws IOException{
        List<String> scopes = new ArrayList<>();
        scopes.add(Constants.SCIM_OAUTH);
        String token = requestAccessToken(getClientId(), scopes);
        LOG.info("token:{} ", token);
        return token;
    }

    public String requestAccessToken(final String clientId, final List<String> scope) throws IOException{
        LOG.info("Request for AccessToken - clientId:{}, scope:{} ", clientId, scope);
        
        //**** Testing properties load - Start
        getProperties();
      //**** Testing properties load - End
        
        String tokenUrl = getTokenEndpoint();
        Token token = getAccessToken(tokenUrl, clientId, scope);
        LOG.debug("oAuth AccessToken response - token:{}", token);
        if (token != null) {
            return token.getAccessToken();
        }
        return null;
    }
    
   
    private Token getAccessToken(final String tokenUrl, final String clientId, final List<String> scopes) throws IOException {
        LOG.info("Access Token Request - tokenUrl:{}, clientId:{}, scopes:{}", tokenUrl, clientId, scopes);

        // Get clientSecret
        String clientSecret = this.getClientDecryptPassword();
        LOG.info("Access Token Request - clientId:{}, clientSecret:{}", clientId, clientSecret);

        // distinct scopes
        Set<String> scopesSet = new HashSet<>(scopes);

        StringBuilder scope = new StringBuilder(ScopeType.OPENID.getValue());
        for (String s : scopesSet) {
            scope.append(" ").append(s);
        }

        LOG.info("Scope required  - {}", scope);

        TokenResponse tokenResponse = requestAccessToken(tokenUrl, clientId, clientSecret,
                scope.toString());
        if (tokenResponse != null) {

            LOG.info("Token Response - tokenScope: {}, tokenAccessToken: {} ", tokenResponse.getScope(),
                    tokenResponse.getAccessToken());
            final String accessToken = tokenResponse.getAccessToken();
            final Integer expiresIn = tokenResponse.getExpiresIn();
            if (Util.allNotBlank(accessToken)) {
                return new Token(null, null, accessToken, ScopeType.OPENID.getValue(), expiresIn);
            }
        }
        return null;
    }
    
    private static TokenResponse requestAccessToken(final String tokenUrl, final String clientId,
            final String clientSecret, final String scope) throws IOException{
        LOG.error("Request for Access Token -  tokenUrl:{}, clientId:{}, clientSecret:{}, scope:{} ", tokenUrl,
                clientId, clientSecret, scope);
        Response response = null;
        try {

            TokenRequest tokenRequest = new TokenRequest(GrantType.CLIENT_CREDENTIALS);
            tokenRequest.setScope(scope);
            tokenRequest.setAuthUsername(clientId);
            tokenRequest.setAuthPassword(clientSecret);
            tokenRequest.setGrantType(GrantType.CLIENT_CREDENTIALS);
            tokenRequest.setAuthenticationMethod(AuthenticationMethod.CLIENT_SECRET_BASIC);
            
            Map<String, String> parameters = new HashMap<>();
            parameters.put("grant_type", GrantType.CLIENT_CREDENTIALS.getValue());
            parameters.put("username", clientId+":"+clientSecret);
            parameters.put("password", clientSecret);
            parameters.put("scope", scope);
            parameters.put("client_id", clientId);
            parameters.put("client_secret", clientSecret);
            parameters.put("authorization_method", AuthenticationMethod.CLIENT_SECRET_BASIC.name());
            
            final MultivaluedHashMap<String, String> multivaluedHashMap = new MultivaluedHashMap<>(
                    parameters);
           // response = request.post(Entity.form(multivaluedHashMap));
            
           HttpClient client = HttpClientBuilder.create().build();
           LOG.info("\n\n\n\n Final try Krishna @@@@@*** Request for Access Token for Post-  tokenUrl:{}, multivaluedHashMap:{} ", tokenUrl,multivaluedHashMap);
           
           try {
               JsonNode jsonNode1 = SimpleHttp.doPost(tokenUrl, client)
                       .header("Authorization","Basic " + tokenRequest.getEncodedCredentials())
                       .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED)
                       .param("grant_type", "client_credentials")
                       .param("username", clientId+":"+clientSecret)
                       .param("scope", "scope")
                       .param("client_id", clientId)
                       .param("client_secret", clientSecret)
                       .param("authorization_method", "client_secret_basic")
                       .asJson();
               LOG.info("\n\n ***** Thankyou Krishna POST Request for Access Token -  jsonNode1:{} ", jsonNode1);

               
           }catch(Exception ex) {
               ex.printStackTrace();
               LOG.error("\n\n\n ********************* Post 1  error is =  "+ex+"*****\n\n\n");
           }
               
                //JsonNode jsonNode2 = SimpleHttp.doPost(tokenUrl, client).header("Authorization","Basic " + tokenRequest.getEncodedCredentials()).header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED).acceptJson().param("user", clientId+":"+clientSecret).json(Entity.form(multivaluedHashMap)).asJson();
           JsonNode jsonNode2 = SimpleHttp.doPost(tokenUrl, client).header("Authorization","Basic " + tokenRequest.getEncodedCredentials()).header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED).json(Entity.form(multivaluedHashMap)).asJson();
           LOG.info("POST Request for Access Token -  jsonNode2:{} ", jsonNode2);

        } finally {

            if (response != null) {
                response.close();
            }
        }
        return null;
    }
    
    /*--------------------------------------------*/
    public static TokenResponse requestAccessToken_TokenRequest(final String tokenUrl, final String clientId,
            final String clientSecret, final String scope) {
        LOG.info(" Latest Request for Access Token -  tokenUrl:{}, clientId:{}, clientSecret:{}, scope:{} ", tokenUrl,
                clientId, clientSecret, scope);
        Response response = null;
        try {
            TokenRequest tokenRequest = new TokenRequest(GrantType.CLIENT_CREDENTIALS);
            tokenRequest.setScope(scope);
            tokenRequest.setAuthUsername(clientId);
            tokenRequest.setAuthPassword(clientSecret);
            Builder request = getClientBuilder(tokenUrl);
            request.header("Authorization", "Basic " + tokenRequest.getEncodedCredentials());
            request.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
            final MultivaluedHashMap<String, String> multivaluedHashMap = new MultivaluedHashMap<>(
                    tokenRequest.getParameters());
            response = request.post(Entity.form(multivaluedHashMap));
            LOG.info("Response for Access Token -  response:{}", response);
            if (response.getStatus() == 200) {
                String entity = response.readEntity(String.class);
                TokenResponse tokenResponse = new TokenResponse();
                tokenResponse.setEntity(entity);
                tokenResponse.injectDataFromJson(entity);
                return tokenResponse;
            }
        } finally {

            if (response != null) {
                response.close();
            }
        }
        return null;
    }
    
    private static TokenResponse requestAccessToken8(final String tokenUrl, final String clientId,
            final String clientSecret, final String scope) throws IOException{
        LOG.error("Request for Access Token -  tokenUrl:{}, clientId:{}, clientSecret:{}, scope:{} ", tokenUrl,
                clientId, clientSecret, scope);
        Response response = null;
        try {

            TokenRequest tokenRequest = new TokenRequest(GrantType.CLIENT_CREDENTIALS);
            tokenRequest.setScope(scope);
            tokenRequest.setAuthUsername(clientId);
            tokenRequest.setAuthPassword(clientSecret);
            tokenRequest.setGrantType(GrantType.CLIENT_CREDENTIALS);
            tokenRequest.setAuthenticationMethod(AuthenticationMethod.CLIENT_SECRET_BASIC);
//            Builder request = getClientBuilder(tokenUrl);
//            request.header("Authorization", "Basic " + tokenRequest.getEncodedCredentials());
//            request.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
            final MultivaluedHashMap<String, String> multivaluedHashMap = new MultivaluedHashMap<>(
                    tokenRequest.getParameters());
           // response = request.post(Entity.form(multivaluedHashMap));
            
           HttpClient client = HttpClientBuilder.create().build();
           LOG.info("\n\n\n\n @@@@@*** Request for Access Token for Post-  tokenUrl:{}, multivaluedHashMap:{} ", tokenUrl,multivaluedHashMap);
           
               
                JsonNode jsonNode2 = SimpleHttp.doPost(tokenUrl, client).header("Authorization","Basic " + tokenRequest.getEncodedCredentials()).header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED).acceptJson().param("user", clientId+":"+clientSecret).json(Entity.form(multivaluedHashMap)).asJson();
           
            LOG.info("POST Request for Access Token -  jsonNode2:{} ", jsonNode2);

        } finally {

            if (response != null) {
                response.close();
            }
        }
        return null;
    }
    
    private static TokenResponse requestAccessToken7(final String tokenUrl, final String clientId,
            final String clientSecret, final String scope) throws IOException{
        LOG.error("Request for Access Token -  tokenUrl:{}, clientId:{}, clientSecret:{}, scope:{} ", tokenUrl,
                clientId, clientSecret, scope);
        Response response = null;
        try {

           Map<String, String> parameters = new HashMap<>();
           parameters.put("grant_type", GrantType.CLIENT_CREDENTIALS.getValue());
           parameters.put("username", clientId+":"+clientSecret);
           parameters.put("password", clientSecret);
           parameters.put("scope", scope);
           parameters.put("client_id", clientId);
           parameters.put("client_secret", clientSecret);
           parameters.put("authorization_method", AuthenticationMethod.CLIENT_SECRET_BASIC.name());
           
            //HttpClient client = HttpClientBuilder.create().build();
           
           HttpClient client = HttpClientBuilder.create().build();
                LOG.info("\n\n\n\n @@@@@*** Request for Access Token for Post-  tokenUrl:{}, parameters:{} ", tokenUrl,parameters);
            //JsonNode jsonNode = SimpleHttp.doGet(tokenUrl, client).authBasic(clientId, clientSecret).json(parameters).asJson();
          /*      try {
            JsonNode jsonNode = SimpleHttp.doGet(tokenUrl, client).acceptJson().param("user", clientId+":"+clientSecret).json(parameters).asJson();
            LOG.info("\n\n GET Request for Access Token -  jsonNode:{} ", jsonNode);
                }catch(Exception ex) {
                    LOG.error("\n\n GET Request for Access Token ex:{} ", ex);
                }
            */
                JsonNode jsonNode2 = SimpleHttp.doPost(tokenUrl, client).header(clientSecret, scope).acceptJson().param("user", clientId+":"+clientSecret).json(parameters).asJson();
            //JsonNode jsonNode = SimpleHttp.doGet(tokenUrl, client).json(parameters).asJson();
            LOG.info("POST Request for Access Token -  jsonNode2:{} ", jsonNode2);

        } finally {

            if (response != null) {
                response.close();
            }
        }
        return null;
    }
    
    
    private static TokenResponse requestAccessToken6(final String tokenEndpoint, final String clientId,
            final String clientSecret, final String scope) throws IOException{
        LOG.error("Request for Access Token -  tokenEndpoint:{}, clientId:{}, clientSecret:{}, scope:{} ", tokenEndpoint,
                clientId, clientSecret, scope);
        Response response = null;
        try {
            TokenRequest tokenRequest = new TokenRequest(GrantType.CLIENT_CREDENTIALS);
            tokenRequest.setScope(scope);
            tokenRequest.setAuthUsername(clientId);
            tokenRequest.setAuthPassword(clientSecret);
            tokenRequest.setAuthenticationMethod(AuthenticationMethod.CLIENT_SECRET_POST);
            LOG.error("Request for Access Token -  tokenRequest:{} ", tokenRequest);           
        
        ResteasyWebTarget webTarget = (ResteasyWebTarget) ClientBuilder.newClient()
                 .target(tokenEndpoint);
        TokenClient tokenClient = webTarget.proxy(TokenClient.class);
         
            //TokenClient tokenClient = new TokenClient(tokenEndpoint);
            tokenClient.setRequest(tokenRequest);
            TokenResponse tokenResponse = tokenClient.exec();
            LOG.error("Request for Access Token -  tokenResponse:{} ", tokenResponse);
           //JsonNode jsonNode = SimpleHttp.doPost(tokenUrl, client).json(multivaluedHashMap).asJson();
           // JsonNode jsonNode = SimpleHttp.doGet(tokenUrl, client).json(parameters).asJson();
            //LOG.error("Request for Access Token -  jsonNode:{} ", jsonNode);
            /*LOG.trace("Response for Access Token -  response:{}", response);
            if (response.getStatus() == 200) {
                String entity = response.readEntity(String.class);
                TokenResponse tokenResponse = new TokenResponse();
                tokenResponse.setEntity(entity);
                tokenResponse.injectDataFromJson(entity);
                return tokenResponse;
            }*/
        } finally {

            if (response != null) {
                response.close();
            }
        }
        return null;
    }
    
    private static TokenResponse requestAccessToken5(final String tokenUrl, final String clientId,
            final String clientSecret, final String scope) throws IOException{
        LOG.error("Request for Access Token -  tokenUrl:{}, clientId:{}, clientSecret:{}, scope:{} ", tokenUrl,
                clientId, clientSecret, scope);
        Response response = null;
        try {
            TokenRequest tokenRequest = new TokenRequest(GrantType.CLIENT_CREDENTIALS);
            tokenRequest.setScope(scope);
            tokenRequest.setAuthUsername(clientId);
            tokenRequest.setAuthPassword(clientSecret);
            tokenRequest.setUsername(clientId+":"+clientSecret);
            
           Map<String, String> parameters = new HashMap<>();
           parameters.put("grant_type", GrantType.CLIENT_CREDENTIALS.getValue());
           parameters.put("username", clientId);
           parameters.put("password", clientSecret);
           parameters.put("scope", scope);
           parameters.put("client_id", clientId);
           parameters.put("client_secret", clientSecret);
           
            HttpClient client = HttpClientBuilder.create().build();
            //final MultivaluedHashMap<String, String> multivaluedHashMap = new MultivaluedHashMap<>(
              //      tokenRequest.getParameters());
            LOG.error("\n\n\n  Request for Access Token -  parameters:{} , {}", parameters,"***\n\n\n");
           //JsonNode jsonNode = SimpleHttp.doPost(tokenUrl, client).json(multivaluedHashMap).asJson();
            JsonNode jsonNode = SimpleHttp.doGet(tokenUrl, client).authBasic(clientId, clientSecret).json(parameters).asJson();
            LOG.error("Request for Access Token -  jsonNode:{} ", jsonNode);
            /*LOG.trace("Response for Access Token -  response:{}", response);
            if (response.getStatus() == 200) {
                String entity = response.readEntity(String.class);
                TokenResponse tokenResponse = new TokenResponse();
                tokenResponse.setEntity(entity);
                tokenResponse.injectDataFromJson(entity);
                return tokenResponse;
            }*/
        } finally {

            if (response != null) {
                response.close();
            }
        }
        return null;
    }
    
    private static TokenResponse requestAccessToken4(final String tokenUrl, final String clientId,
            final String clientSecret, final String scope) throws IOException{
        LOG.error("Request for Access Token -  tokenUrl:{}, clientId:{}, clientSecret:{}, scope:{} ", tokenUrl,
                clientId, clientSecret, scope);
        Response response = null;
        try {
            TokenRequest tokenRequest = new TokenRequest(GrantType.CLIENT_CREDENTIALS);
            tokenRequest.setScope(scope);
            tokenRequest.setAuthUsername(clientId);
            tokenRequest.setAuthPassword(clientSecret);
            tokenRequest.setUsername(clientId+":"+clientSecret);
            
           Map<String, String> parameters = new HashMap<>();
           parameters.put("grant_type", GrantType.CLIENT_CREDENTIALS.getValue());
           parameters.put("username", clientId+":"+clientSecret);
           parameters.put("scope", scope);
           parameters.put("client_id", clientId);
           parameters.put("client_secret", clientSecret);
           
            HttpClient client = HttpClientBuilder.create().build();
            //final MultivaluedHashMap<String, String> multivaluedHashMap = new MultivaluedHashMap<>(
              //      tokenRequest.getParameters());
            LOG.error("Request for Access Token -  parameters:{} ", parameters);
           //JsonNode jsonNode = SimpleHttp.doPost(tokenUrl, client).json(multivaluedHashMap).asJson();
            JsonNode jsonNode = SimpleHttp.doGet(tokenUrl, client).json(parameters).asJson();
            LOG.error("Request for Access Token -  jsonNode:{} ", jsonNode);
            /*LOG.trace("Response for Access Token -  response:{}", response);
            if (response.getStatus() == 200) {
                String entity = response.readEntity(String.class);
                TokenResponse tokenResponse = new TokenResponse();
                tokenResponse.setEntity(entity);
                tokenResponse.injectDataFromJson(entity);
                return tokenResponse;
            }*/
        } finally {

            if (response != null) {
                response.close();
            }
        }
        return null;
    }

    private static TokenResponse requestAccessToken3(final String tokenUrl, final String clientId,
            final String clientSecret, final String scope) throws IOException{
        LOG.debug("Request for Access Token -  tokenUrl:{}, clientId:{}, clientSecret:{}, scope:{} ", tokenUrl,
                clientId, clientSecret, scope);
        Response response = null;
        try {
            TokenRequest tokenRequest = new TokenRequest(GrantType.CLIENT_CREDENTIALS);
            tokenRequest.setScope(scope);
            tokenRequest.setAuthUsername(clientId);
            tokenRequest.setAuthPassword(clientSecret);
            /*
             * Builder request = getClientBuilder(tokenUrl); request.header("Authorization",
             * "Basic " + tokenRequest.getEncodedCredentials());
             * request.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED); final
             * MultivaluedHashMap<String, String> multivaluedHashMap = new
             * MultivaluedHashMap<>( tokenRequest.getParameters()); response =
             * request.post(Entity.form(multivaluedHashMap));
             */
            
            HttpClient client = HttpClientBuilder.create().build();
            
            response = SimpleHttp.doPost(tokenUrl, client).header("Authorization", "Basic " + tokenRequest.getEncodedCredentials()).authBasic(clientId,clientSecret).json(tokenRequest).asJson(Response.class);
            //response = SimpleHttp.doPost(tokenUrl, client).json(tokenRequest).asJson(Response.class);
                  
            //java.lang.RuntimeException: java.lang.ClassNotFoundException: Provider for jakarta.ws.rs.ext.RuntimeDelegate cannot be found
          
            LOG.trace("Response for Access Token -  response:{}", response);
            if (response.getStatus() == 200) {
                String entity = response.readEntity(String.class);
                TokenResponse tokenResponse = new TokenResponse();
                tokenResponse.setEntity(entity);
                tokenResponse.injectDataFromJson(entity);
                return tokenResponse;
            }
        } finally {

            if (response != null) {
                response.close();
            }
        }
        return null;
    }
    
    

    private static TokenResponse requestAccessToken2(final String tokenUrl, final String clientId,
            final String clientSecret, final String scope) throws IOException{
        LOG.debug("Request for Access Token -  tokenUrl:{}, clientId:{}, clientSecret:{}, scope:{} ", tokenUrl,
                clientId, clientSecret, scope);
        Response response = null;
        try {
            TokenRequest tokenRequest = new TokenRequest(GrantType.CLIENT_CREDENTIALS);
            tokenRequest.setScope(scope);
            tokenRequest.setAuthUsername(clientId);
            tokenRequest.setAuthPassword(clientSecret);
            /*
             * Builder request = getClientBuilder(tokenUrl); request.header("Authorization",
             * "Basic " + tokenRequest.getEncodedCredentials());
             * request.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED); final
             * MultivaluedHashMap<String, String> multivaluedHashMap = new
             * MultivaluedHashMap<>( tokenRequest.getParameters()); response =
             * request.post(Entity.form(multivaluedHashMap));
             */
            
            HttpClient client = HttpClientBuilder.create().build();
            
            response = SimpleHttp.doPost(tokenUrl, client).header("Authorization", "Basic " + tokenRequest.getEncodedCredentials()).authBasic(clientId,clientSecret).json(tokenRequest).asJson(Response.class);
            //response = SimpleHttp.doPost(tokenUrl, client).json(tokenRequest).asJson(Response.class);
                  
            //java.lang.RuntimeException: java.lang.ClassNotFoundException: Provider for jakarta.ws.rs.ext.RuntimeDelegate cannot be found
          
            LOG.trace("Response for Access Token -  response:{}", response);
            if (response.getStatus() == 200) {
                String entity = response.readEntity(String.class);
                TokenResponse tokenResponse = new TokenResponse();
                tokenResponse.setEntity(entity);
                tokenResponse.injectDataFromJson(entity);
                return tokenResponse;
            }
        } finally {

            if (response != null) {
                response.close();
            }
        }
        return null;
    }
    
    private static TokenResponse requestAccessToken1(final String tokenUrl, final String clientId,
            final String clientSecret, final String scope) {
        LOG.debug("Request for Access Token -  tokenUrl:{}, clientId:{}, clientSecret:{}, scope:{} ", tokenUrl,
                clientId, clientSecret, scope);
        Response response = null;
        try {
            TokenRequest tokenRequest = new TokenRequest(GrantType.CLIENT_CREDENTIALS);
            tokenRequest.setScope(scope);
            tokenRequest.setAuthUsername(clientId);
            tokenRequest.setAuthPassword(clientSecret);
            Builder request = getClientBuilder(tokenUrl);
            request.header("Authorization", "Basic " + tokenRequest.getEncodedCredentials());
            request.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
            final MultivaluedHashMap<String, String> multivaluedHashMap = new MultivaluedHashMap<>(
                    tokenRequest.getParameters());
            response = request.post(Entity.form(multivaluedHashMap));
            LOG.trace("Response for Access Token -  response:{}", response);
            if (response.getStatus() == 200) {
                String entity = response.readEntity(String.class);
                TokenResponse tokenResponse = new TokenResponse();
                tokenResponse.setEntity(entity);
                tokenResponse.injectDataFromJson(entity);
                return tokenResponse;
            }
        } finally {

            if (response != null) {
                response.close();
            }
        }
        return null;
    }
    

}
