package io.jans.idp.keycloak.service;

import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import io.jans.as.client.TokenRequest;
import io.jans.as.common.util.AttributeConstants;
import io.jans.as.model.common.AuthenticationMethod;
import io.jans.as.model.common.GrantType;
import io.jans.idp.keycloak.util.JansUtil;
import io.jans.scim.model.scim2.user.UserResource;

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

import com.fasterxml.jackson.databind.JsonNode;

public class CredentialAuthenticatingService {

    private static Logger LOG = LoggerFactory.getLogger(CredentialAuthenticatingService.class);
    private static JansUtil jansUtil = new JansUtil();

   public boolean authenticateUser(final String clientId, final String clientSecret) {
        LOG.debug("CredentialAuthenticatingService -  clientId:{}, clientSecret:{} ", clientId, clientSecret);
        boolean isValid = false;
        try {

            String token = jansUtil.requestAccessToken(jansUtil.getTokenEndpoint(), clientId, clientSecret, null,
                    GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS, AuthenticationMethod.CLIENT_SECRET_POST,
                    MediaType.APPLICATION_FORM_URLENCODED);

            LOG.info("Final token token  - {}", token);
           
            isValid = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error("\n\n\n ********************* Post error is =  " + ex + "*****\n\n\n");
        }
        return isValid;
    }

}
