package io.jans.idp.keycloak.service;


import io.jans.as.model.common.AuthenticationMethod;
import io.jans.as.model.common.GrantType;
import io.jans.idp.keycloak.util.JansUtil;

import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;

public class CredentialAuthenticatingService {

    private static Logger LOG = LoggerFactory.getLogger(CredentialAuthenticatingService.class);
    private static JansUtil jansUtil = new JansUtil();

   public boolean authenticateUser(final String username, final String password) {
        LOG.info("CredentialAuthenticatingService::authenticateUser() -  username:{}, password:{} ", username, password);
        boolean isValid = false;
        try {

            String token = jansUtil.requestUserToken(jansUtil.getTokenEndpoint(), username, password, null,
                    GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS, AuthenticationMethod.CLIENT_SECRET_POST,
                    MediaType.APPLICATION_FORM_URLENCODED);

            LOG.info("CredentialAuthenticatingService::authenticateUser() -  Final token token  - {}", token);
           
            isValid = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error("\n\n\n ********************* CredentialAuthenticatingService::authenticateUser() -   Error while authenticating is :{}  " + ex + "*****\n\n\n");
        }
        return isValid;
    }

}
