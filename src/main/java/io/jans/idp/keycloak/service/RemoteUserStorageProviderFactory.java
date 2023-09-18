package io.jans.idp.keycloak.service;

import io.jans.idp.keycloak.util.Constants;
import io.jans.idp.keycloak.config.JansConfigSource;
import org.keycloak.component.ComponentValidationException;
import io.jans.util.exception.InvalidConfigurationException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

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
    protected String jansProperties = null;
    protected JansConfigSource jansConfigSource = null;

    public static final String PROVIDER_NAME = "jans-keycloak-storage-api";
       
    @Override
    public RemoteUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        LOG.info("\n\n\n RemoteUserStorageProviderFactory::create() - session:{}, model:{}",session, model);
        jansConfigSource = new JansConfigSource(jansProperties);
        return new RemoteUserStorageProvider(session, model);
    }
    
    @Override
    public String getId() {
        String id = PROVIDER_NAME;
        LOG.info("id:{}",id);
        
        return id;
    }
    
    @Override
    public void init(Config.Scope config) {
        LOG.info("\n\n\n RemoteUserStorageProviderFactory::init() - config:{}",config);
      
        this.jansProperties =  System.getProperty(Constants.JANS_CONFIG_PROP_PATH);
        if(StringUtils.isBlank(jansProperties)) {
            throw new ComponentValidationException("Configuration property file path `System property` not set, please verify.");
        }
        LOG.info("\n\n\n RemoteUserStorageProviderFactory::init() - jansProperties:{}",jansProperties);
        
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        LOG.info("\n\n\n RemoteUserStorageProviderFactory::postInit() - config:{}",factory);
        
    }

    @Override
    public void close() {
        LOG.info("\n\n\n RemoteUserStorageProviderFactory::close() - Exit:{}");
        
    }

   
}
