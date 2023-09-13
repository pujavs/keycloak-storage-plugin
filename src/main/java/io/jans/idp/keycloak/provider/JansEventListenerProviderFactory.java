package io.jans.idp.keycloak.provider;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import jakarta.ws.rs.WebApplicationException;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JansEventListenerProviderFactory implements EventListenerProviderFactory  {

 public static final String PROVIDER_NAME = "jans-keycloak-storage-api";
    private static Logger LOG = LoggerFactory.getLogger(JansEventListenerProviderFactory.class);
       private String tokenUrl;
    private String clientId;
    private String password;

    @Override
    public EventListenerProvider create(KeycloakSession keycloakSession) {

        return new JansEventListenerProvider(tokenUrl, clientId, password);
    }

    @Override
    public void init(Config.Scope config) {
       LOG.info("\n\n\n JansEventListenerProviderFactory::init() - config:{}",config);
        System.out.println("\n\n\n ** RemoteUserStorageProvider::init()- config = "+config );
        
        tokenUrl = config.get("jans-token-url");
        clientId = config.get("jans-client-id");
        
        LOG.info("\n\n\n ***** JansEventListenerProviderFactory::init() - Properties form Config.Scope - tokenUrl:{}, clientId:{}",tokenUrl, clientId);
      
        
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return PROVIDER_NAME;
    }
}