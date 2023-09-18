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
import jakarta.ws.rs.WebApplicationException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.*;


import org.apache.commons.codec.binary.Base64;
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
    private JansConfigSource jansConfigSource = new JansConfigSource();
    private Map<String, String> configProperties = null;
    
    public JansUtil() {
        configProperties = getProperties() ;
        if(configProperties==null || configProperties.isEmpty()) {
           throw new WebApplicationException("Config properties is null!!!"); 
        }
    }
    
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
        return configProperties.get("token.endpoint");
    }
   
    public String getScimUserEndpoint() {
        return configProperties.get("scim.user.endpoint");
    }
    
    public String getScimUserSearchEndpoint() {
        return configProperties.get("scim.user.search.endpoint");
    }
    
    public String getClientId() {
        return configProperties.get("client.id");
    }
    
    public String getClientPassword() {
        return configProperties.get("client.password");
    }
    
    public String getScimOauthScope() {
        return configProperties.get("scim.oauth.scope");
    }
    
    public static Builder getClientBuilder(String url) {
        return ClientBuilder.newClient().target(url).request();
    }
    
    public String requestScimAccessToken() throws IOException{
        List<String> scopes = new ArrayList<>();
        scopes.add(getScimOauthScope());
        String token = requestAccessToken(getClientId(), scopes);
        LOG.info("token:{} ", token);
        return token;
    }

    public String requestAccessToken(final String clientId, final List<String> scope) throws IOException{
        LOG.info("Request for AccessToken - clientId:{}, scope:{} ", clientId, scope);
        
        String tokenUrl = getTokenEndpoint();
        String token = getAccessToken(tokenUrl, clientId, scope);
        LOG.debug("oAuth AccessToken response - token:{}", token);
       
        return token;
    }
    
   
    public String getAccessToken(final String tokenUrl, final String clientId, final List<String> scopes) throws IOException {
        LOG.info("Access Token Request - tokenUrl:{}, clientId:{}, scopes:{}", tokenUrl, clientId, scopes);

        // Get clientSecret
        String clientSecret = this.getClientPassword();
        LOG.info("Access Token Request - clientId:{}, clientSecret:{}", clientId, clientSecret);

        // distinct scopes
        Set<String> scopesSet = new HashSet<>(scopes);

        StringBuilder scope = new StringBuilder(ScopeType.OPENID.getValue());
        for (String s : scopesSet) {
            scope.append(" ").append(s);
        }

        LOG.info("Scope required  - {}", scope);

        String token = requestAccessToken(tokenUrl, clientId, clientSecret,scope.toString(),GrantType.CLIENT_CREDENTIALS,AuthenticationMethod.CLIENT_SECRET_BASIC,MediaType.APPLICATION_FORM_URLENCODED);
        LOG.info("Final token token  - {}", token);
        return token;
    }
    
    public String requestAccessToken(final String tokenUrl, final String clientId,
            final String clientSecret, final String scope,GrantType grantType, AuthenticationMethod authenticationMethod, String mediaType) throws IOException{
        LOG.debug("Request for Access Token -  tokenUrl:{}, clientId:{}, clientSecret:{}, scope:{}, grantType:{}, authenticationMethod:{}, mediaType:{}", tokenUrl,
                clientId, clientSecret, scope, grantType, authenticationMethod, mediaType);
        String token = null;
        try {

            TokenRequest tokenRequest = new TokenRequest(grantType);
            tokenRequest.setScope(scope);
            tokenRequest.setAuthUsername(clientId);
            tokenRequest.setAuthPassword(clientSecret);
            tokenRequest.setGrantType(grantType);
            tokenRequest.setAuthenticationMethod(authenticationMethod);
            
           LOG.debug("  tokenRequest.getEncodedCredentials():{}, this.getEncodedCredentials():{}", tokenRequest.getEncodedCredentials(),this.getEncodedCredentials(clientId,clientSecret));     
           HttpClient client = HttpClientBuilder.create().build();
           JsonNode jsonNode = SimpleHttp.doPost(tokenUrl, client)
                       .header("Authorization","Basic " + tokenRequest.getEncodedCredentials())
                       .header("Content-Type", mediaType)
                       .param("grant_type", "client_credentials")
                       .param("username", clientId+":"+clientSecret)
                       .param("scope",scope)
                       .param("client_id", clientId)
                       .param("client_secret", clientSecret)
                       .param("authorization_method", "client_secret_basic")
                       .asJson();
               LOG.info("\n\n ***** POST Request for Access Token -  jsonNode:{} ", jsonNode);

               //if(validateTokenScope(jsonNode,scope)) {
               token = this.getToken(jsonNode);
              // }
               LOG.info("\n\n ***** POST Request for Access Token -  token:{} ", token);

               
           }catch(Exception ex) {
               ex.printStackTrace();
               LOG.error("\n\n\n ********************* Post error is =  "+ex+"*****\n\n\n");
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
    
    private boolean hasCredentials(String authUsername, String authPassword) {
        return(StringUtils.isNotBlank(authUsername) && StringUtils.isNotBlank(authPassword)) ;
    }
    
    /**
     * Returns the client credentials (URL encoded).
     *
     * @return The client credentials.
     */
    private String getCredentials(String authUsername, String authPassword) throws UnsupportedEncodingException {
        LOG.info("getCredentials() - authUsername:{}, authPassword:{}", authUsername, authPassword);
        return URLEncoder.encode(authUsername, Util.UTF8_STRING_ENCODING)
                + ":"
                + URLEncoder.encode(authPassword, Util.UTF8_STRING_ENCODING);
    }
   
    private String getEncodedCredentials(String authUsername, String authPassword) {
        LOG.info("getEncodedCredentials() - authUsername:{}, authPassword:{}", authUsername, authPassword);
        try {
            if (hasCredentials(authUsername,authPassword)) {
                return Base64.encodeBase64String(Util.getBytes(getCredentials(authUsername,authPassword)));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }
    
    

}
