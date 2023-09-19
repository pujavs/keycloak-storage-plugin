package io.jans.idp.keycloak.service;

import io.jans.scim.model.scim2.user.UserResource;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.UserStorageProvider;

import org.keycloak.storage.user.UserLookupProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteUserStorageProvider implements CredentialInputValidator, UserLookupProvider, UserStorageProvider {

    private static Logger LOG = LoggerFactory.getLogger(RemoteUserStorageProvider.class);
    private static String AUTH_USER_ENDPOINT = "http://localhost:8080/jans-config-api/mgt/configuser/";

    private KeycloakSession session;
    private ComponentModel model;
    private UsersApiLegacyService usersService;
    private CredentialAuthenticatingService credentialAuthenticatingService = new CredentialAuthenticatingService();

    public RemoteUserStorageProvider(KeycloakSession session, ComponentModel model) {
        LOG.info(" session:{}, model:{}", session, model);

        this.session = session;
        this.model = model;
        this.usersService = new UsersApiLegacyService(session, model);
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        LOG.info(" supportsCredentialType() - credentialType:{}", credentialType);
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return user.credentialManager().isConfiguredFor(credentialType);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {
        LOG.info(" isValid() - realm:{}, user:{}, credentialInput:{}, user.getUsername():{}, ", realm, user,
                credentialInput, user.getUsername(), credentialInput.getChallengeResponse());
        boolean verifyPasswordResponse = credentialAuthenticatingService.authenticateUser(user.getUsername(),
                credentialInput.getChallengeResponse());

        /// if (verifyPasswordResponse == null)
        return verifyPasswordResponse;

        // return verifyPasswordResponse.getResult();
    }

    /**
     * Get user based on id
     */
    public UserModel getUserById(RealmModel paramRealmModel, String id) {
        LOG.info("getUserById() paramRealmModel:{}, id:{}", paramRealmModel, id);

        UserModel userModel = null;
        try {
            UserResource user = usersService.getUserById(id);
            LOG.info("***** user fetched based on  id:{} is user:{}", id, user);
            if (user != null) {
                userModel = createUserModel(paramRealmModel, user);
                LOG.info("\n\n\n\n ***************** New userModel =" + userModel + "\n\n");

                LOG.info("userModel:{}", userModel);
            }

            LOG.info("User fetched with id:{} from external service is:{}", id, user);

        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error("Error fetching user id:{} from external service is:{} - {} ", id, ex.getMessage(), ex);

        }
        return userModel;
    }

    /**
     * Get user based on name
     */
    public UserModel getUserByUsername(RealmModel paramRealmModel, String name) {
        LOG.info("getUserByUsername() paramRealmModel:{}, name:{}", paramRealmModel, name);

        UserModel userModel = null;
        try {
            UserResource user = usersService.getUserByName(name);
            LOG.info("User fetched with name:{} from external service is:{}", name, user);

        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error("\n Error fetching user name:{}, from external service is:{} - {} ", name, ex.getMessage(), ex);

        }
        return userModel;
    }

    public UserModel getUserByEmail(RealmModel paramRealmModel, String paramString) {
        return null;
    }

    public void close() {
        LOG.info("createUserModel()::close()");

    }

    private UserModel createUserModel(RealmModel realm, UserResource user) {
        LOG.info("\n\n\n createUserModel() - realm:{} , user:{}", realm, user + "\n\n\n");

        UserModel userModel = new UserAdapter(session, realm, model, user);
        LOG.info("\n\n\n Final createUserModel() - userModel:{}", userModel);

        return userModel;
    }
}
