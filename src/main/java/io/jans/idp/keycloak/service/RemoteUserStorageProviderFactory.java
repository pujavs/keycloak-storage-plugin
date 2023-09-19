package io.jans.idp.keycloak.service;

import io.jans.idp.keycloak.config.JansConfigSource;
import io.jans.idp.keycloak.util.Constants;

import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.storage.UserStorageProviderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteUserStorageProviderFactory implements UserStorageProviderFactory<RemoteUserStorageProvider> {

    private static Logger LOG = LoggerFactory.getLogger(RemoteUserStorageProviderFactory.class);
    public static final String PROVIDER_NAME = "jans-keycloak-storage-api";
    // initialize config properties
    //JansConfigSource jansConfigSource = new JansConfigSource();

    @Override
    public RemoteUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        LOG.info("\n\n\n RemoteUserStorageProviderFactory::create() - session:{}, model:{}", session, model);
        return new RemoteUserStorageProvider(session, model);
    }

    @Override
    public String getId() {
        String id = PROVIDER_NAME;
        LOG.info("id:{}", id);

        return id;
    }

    @Override
    public void init(Config.Scope config) {
        LOG.info(
                "\n\n\n RemoteUserStorageProviderFactory::init() - config:{}, System.getProperty(Constants.JANS_CONFIG_PROP_PATH):{}",
                config, System.getProperty(Constants.JANS_CONFIG_PROP_PATH));
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        LOG.info("\n\n\n RemoteUserStorageProviderFactory::postInit() - config:{}", factory);

    }

    @Override
    public void close() {
        LOG.info("\n\n\n RemoteUserStorageProviderFactory::close() - Exit:{}");

    }

}
