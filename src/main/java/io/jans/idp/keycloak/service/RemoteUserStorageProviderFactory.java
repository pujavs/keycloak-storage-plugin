package io.jans.idp.keycloak.service;

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
    protected Properties properties = new Properties();

    public static final String PROVIDER_NAME = "jans-keycloak-storage-api";
       
    @Override
    public RemoteUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        LOG.info("\n\n\n RemoteUserStorageProviderFactory::create() - session:{}, model:{}",session, model);
        
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
        InputStream is = getClass().getClassLoader().getResourceAsStream("users.properties");
        LOG.error("\n\n !!1. users.properties in is = "+is);
        
        
     

        is = getClass().getClassLoader().getResourceAsStream("jans-keycloak-storage-api.properties");
        LOG.error("\n\n !!2.jans-keycloak-storage-api in is = "+is);
      
        if (is == null) {
            LOG.error("\n\n\n Could not find users.properties in classpath!!!! \n\n");
        } else {
            try {
                LOG.error("\n\n\n !!!Found users.properties in classpath!!!! \n\n");
                properties.load(is);
                LOG.error("\n\n\n RemoteUserStorageProviderFactory::init() - properties = "+properties+"\n\n");
               
             
                
            } catch (IOException ex) {
                LOG.error("Failed to load users.properties file", ex);
            }
        }
        String tokenUrl = config.get("jans-token-url");
        String clientId = config.get("jans-client-id");
        
        LOG.info("\n\n\n ***** RemoteUserStorageProviderFactory::init() - Properties form Config.Scope - tokenUrl:{}, clientId:{}",tokenUrl, clientId);
        
        readFile();
        
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        LOG.info("\n\n\n RemoteUserStorageProviderFactory::postInit() - config:{}",factory);
        
    }

    @Override
    public void close() {
        LOG.info("\n\n\n RemoteUserStorageProviderFactory::close() - Exit:{}");
        
    }

    private void readFile() {
        LOG.info("\n\n\n ***** RemoteUserStorageProviderFactory::readFile() - Properties form Config.Scope ");

        try {
            String path = System.getProperty("jans.props.path");
            LOG.info("\n\n\n ***** RemoteUserStorageProviderFactory::readFile() - path:{}", path);

            if (path != null && path.trim().length() > 0) {
                // to load application's properties, we use this class
                properties = new Properties();

                FileInputStream file;

                // the base folder is ./, the root of the main.properties file
                String filePath = path+"\\jans-keycloak-storage-api.properties";

                // load the file handle for main.properties
                file = new FileInputStream(filePath);
                LOG.info("\n\n\n ***** RemoteUserStorageProviderFactory::readFile() - file =" + file + "\n\n");

                if (file != null) {
                    LOG.info("\n\n\n ***** RemoteUserStorageProviderFactory::readFile() - loading file \n\n");
                    // load all the properties from this file
                    properties.load(file);
                    LOG.info("\n\n\n ***** RemoteUserStorageProviderFactory::readFile() - properties =" + properties + "\n\n");
                    
                    if(properties!=null) {
                        printProperties(properties);
                    }
                    // we have loaded the properties, so close the file handle
                    file.close();
                }
            }
        } catch (IOException ex) {
            LOG.error("Failed to load  file", ex);
        }

    }
    

public static void printProperties(Properties prop) {
    prop.keySet().stream()
            .map(key -> key + ": " + prop.getProperty(key.toString()))
            .forEach(System.out::println);
}
}
