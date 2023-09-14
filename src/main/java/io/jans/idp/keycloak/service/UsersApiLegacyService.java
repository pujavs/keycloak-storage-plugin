package io.jans.idp.keycloak.service;

import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import io.jans.as.common.util.AttributeConstants;
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


public class UsersApiLegacyService {

    private static Logger LOG = LoggerFactory.getLogger(UsersApiLegacyService.class);
    private ScimService scimService = new ScimService();
    
    private KeycloakSession session;
    private ComponentModel model;
    
    public UsersApiLegacyService(KeycloakSession session,ComponentModel model) {
        LOG.info(" session:{}, model:{}", session, model);
        
        this.session = session;
        this.model = model;
    }
    
    public UserResource getUserById(String inum) {
        LOG.info(" inum:{}", inum);
        
        try {
            
            return scimService.getUserById(inum);
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error("Error fetching user based on inum:{} from external service is:{} - {} ", inum, ex.getMessage(), ex);
            
        }
        return null;
    }
        
    public UserResource getUserByName(String username) {
        LOG.info(" username:{}", username);
        try {
            
            return scimService.getUserByName(username);
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error("Error fetching user based on username:{} from external service is:{} - {} ", username, ex.getMessage(), ex);
            
        }
        return null;
    }
    
    public UserResource getUserByEmail(String email) {
        LOG.info(" email:{}", email);
        try {
            
            return scimService.getUserByEmail(email);
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error("Error fetching user based on email:{} from external service is:{} - {} ", email, ex.getMessage(), ex);
            
        }
        return null;
    }
    
    
    

}
