package io.jans.idp.keycloak.service;

import io.jans.util.exception.InvalidConfigurationException;

import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.LegacyUserCredentialManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.adapter.AbstractUserAdapter;
import org.keycloak.storage.UserStorageProviderFactory;

import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;



import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteUserStorageProviderFactory implements UserStorageProviderFactory<RemoteUserStorageProvider> {
   
    private static Logger LOG = LoggerFactory.getLogger(RemoteUserStorageProviderFactory.class);
   

    public static final String PROVIDER_NAME = "jans-keycloak-storage-api";
       
    @Override
    public RemoteUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        LOG.info("\n\n\n RemoteUserStorageProviderFactory::create() - session:{}, model:{}",session, model);
        System.out.println("\n\n\n ** RemoteUserStorageProvider::create()- session = "+session+" ,model = "+model );
        return new RemoteUserStorageProvider(session, model);
    }
    
    @Override
    public String getId() {
        String id = PROVIDER_NAME;
        LOG.info("id:{}",id);
        System.out.println("id = "+id);
        return id;
    }
    
    @Override
    public void init(Config.Scope config) {
        LOG.info("\n\n\n RemoteUserStorageProviderFactory::init() - config:{}",config);
        System.out.println("\n\n\n ** RemoteUserStorageProvider::init()- config = "+config );
        
        String tokenUrl = config.get("jans-token-url");
        String clientId = config.get("jans-client-id");
        
        LOG.info("\n\n\n ***** RemoteUserStorageProviderFactory::init() - Properties form Config.Scope - tokenUrl:{}, clientId:{}",tokenUrl, clientId);
      
        
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        LOG.info("\n\n\n RemoteUserStorageProviderFactory::postInit() - config:{}",factory);
        System.out.println("\n\n\n ** RemoteUserStorageProvider::postInit()- config = "+factory );
    }

    @Override
    public void close() {
        LOG.info("\n\n\n RemoteUserStorageProviderFactory::close() - Exit:{}");
        System.out.println("\n\n\n ** RemoteUserStorageProvider::close()- **" );
    }

}
