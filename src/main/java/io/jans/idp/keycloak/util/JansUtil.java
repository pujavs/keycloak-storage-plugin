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
import io.jans.scim.model.scim2.user.UserResource;
import io.jans.idp.keycloak.client.JansTokenClient;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;


import java.io.IOException;
import java.util.*;
import java.util.stream.*;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.util.JsonSerialization;
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
        LOG.debug("\n\n *** JansUtil::Getting properties \n\n");
        Map<String, String> props = jansConfigSource.getProperties();
        LOG.debug("\n\n JansUtil::getProperties:{}",props);
        
        LOG.debug("\n\n JansUtil::getProperties props.get(token.endpoint):{}",props.get("token.endpoint"));
        
        getPropertyNames();
        return props;
    }
    
    public Set<String> getPropertyNames() {
        LOG.debug(" \n\n\n ***  JansUtil - getPropertyNames():{}", jansConfigSource.getPropertyNames());
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
        String token = getAccessToken(tokenUrl, clientId, scope);
        LOG.debug("oAuth AccessToken response - token:{}", token);
       
        return token;
    }
    
   
    private String getAccessToken(final String tokenUrl, final String clientId, final List<String> scopes) throws IOException {
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

        String token = requestAccessToken(tokenUrl, clientId, clientSecret,scope.toString());
        LOG.info("Final token token  - {}", token);
        return token;
    }
    
    private String requestAccessToken(final String tokenUrl, final String clientId,
            final String clientSecret, final String scope) throws IOException{
        LOG.debug("Request for Access Token -  tokenUrl:{}, clientId:{}, clientSecret:{}, scope:{} ", tokenUrl,
                clientId, clientSecret, scope);
        String token = null;
        try {

            TokenRequest tokenRequest = new TokenRequest(GrantType.CLIENT_CREDENTIALS);
            tokenRequest.setScope(scope);
            tokenRequest.setAuthUsername(clientId);
            tokenRequest.setAuthPassword(clientSecret);
            tokenRequest.setGrantType(GrantType.CLIENT_CREDENTIALS);
            tokenRequest.setAuthenticationMethod(AuthenticationMethod.CLIENT_SECRET_BASIC);
            
                      
           HttpClient client = HttpClientBuilder.create().build();
           JsonNode jsonNode = SimpleHttp.doPost(tokenUrl, client)
                       .header("Authorization","Basic " + tokenRequest.getEncodedCredentials())
                       .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED)
                       .param("grant_type", "client_credentials")
                       .param("username", clientId+":"+clientSecret)
                       .param("scope",scope)
                       .param("client_id", clientId)
                       .param("client_secret", clientSecret)
                       .param("authorization_method", "client_secret_basic")
                       .asJson();
               LOG.info("\n\n ***** Thankyou Dearest Krishna POST Request for Access Token -  jsonNode:{} ", jsonNode);

               //if(validateTokenScope(jsonNode,scope)) {
               token = this.getToken(jsonNode);
              // }
               LOG.info("\n\n ***** Thankyou Dearest Raaha-Krishna POST Request for Access Token -  token:{} ", token);

               
           }catch(Exception ex) {
               ex.printStackTrace();
               LOG.error("\n\n\n ********************* Post 1  error is =  "+ex+"*****\n\n\n");
           }
        return token;
    }
    
   
    private boolean validateTokenScope(JsonNode jsonNode,String scope) {
        
        LOG.info(" \n\n validateTokenScope() - jsonNode:{}, scope:{}", jsonNode, scope);
        boolean validScope = false;
        try {
            
            List<String> scopeList = Stream.of(scope.split(" ", -1))
                    .collect(Collectors.toList());

            if (jsonNode != null && jsonNode.get("scope")!=null) {
                    JsonNode value = jsonNode.get("scope");
                    LOG.info("\n\n *** validateTokenScope() - value:{}, value.getClass():{}", value, value.getClass());
                    
                    if(value!=null) {
                        String responseScope = value.toString(); 
                        LOG.info("validateTokenScope() - scope:{}, responseScope:{}, responseScope.contains(scope):{}", scope, responseScope, responseScope.contains(scope));
                        if(responseScope.contains(scope)){
                            validScope = true;
                        }
                    }
                   
                }
            LOG.info("validateTokenScope() - validScope:{}", validScope);

        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error("\n\n Error while validating token scope from response is ex:{}", ex);
        }
        return validScope;
        
    }
    
    private String getToken(JsonNode jsonNode) {
        LOG.info(" \n\n getToken() - jsonNode:{}", jsonNode);
        
        String token = null;
        try {
            
            if (jsonNode != null) { 
                if(jsonNode.get("access_token")!=null) {
                    JsonNode value = jsonNode.get("access_token");
                    LOG.info("\n\n *** getToken() - value:{}, value.getClass():{}", value, value.getClass());
                    
                    if(value!=null) {
                        token = value.asText(); 
                    }
                    LOG.info("getToken() - token:{}", token);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error("\n\n Error while getting token from response is ex:{}", ex);
        }
        return token;
    }
   

}
