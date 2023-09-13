package io.jans.idp.keycloak.service;

import io.jans.scim.model.scim2.user.UserResource;

import java.io.IOException;
import java.util.List;

import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.LegacyUserCredentialManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.adapter.AbstractUserAdapter;
import org.keycloak.storage.user.UserLookupProvider;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSessionFactory;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteUserStorageProvider implements UserLookupProvider, UserStorageProvider {

    private static Logger LOG = LoggerFactory.getLogger(RemoteUserStorageProvider.class);
    private static String AUTH_USER_ENDPOINT = "http://localhost:8080/jans-config-api/mgt/configuser/";

    private KeycloakSession session;
    private ComponentModel model;
    private UsersApiLegacyService usersService;

    public RemoteUserStorageProvider(KeycloakSession session, ComponentModel model) {
        LOG.info(" session:{}, model:{}", session, model);
        System.out.println(" \n\n session="+session+", model="+model+", usersService="+usersService+"\n\n" );

        this.session = session;
        this.model = model;
        this.usersService = new UsersApiLegacyService(session, model);
    }

    /**
     * Get user based on id
     */
    public UserModel getUserById(RealmModel paramRealmModel, String id) {
        LOG.info("getUserById() paramRealmModel:{}, id:{}", paramRealmModel, id);
        System.out.println("\n getUserById() using paramRealmModel = "+paramRealmModel+" , id="+id+"\n\n" );
        UserModel userModel = null;
        try {
            UserResource user = usersService.getUserById(id);
            LOG.info("***** user fetched based on  id:{} is user:{}", id, user);
            if (user != null) {
                userModel = createUserModel(paramRealmModel, user);
                LOG.info("\n\n\n\n ***************** New userModel ="+userModel+"\n\n");
                System.out.println(userModel.toString());
                LOG.info("userModel:{}", userModel);
            }
         
            LOG.info("User fetched with id:{} from external service is:{}", id, user);
            System.out.println("\n getUserById()- User fetched with id ="+id+" from external service is="+user+"\n\n");

        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error("Error fetching user id:{} from external service is:{} - {} ", id, ex.getMessage(), ex);
            System.out.println("\n getUserById()- Error fetching user id ="+id+" from external service is ="+ex+"\n\n");
        }
        return userModel;
    }

    /**
     * Get user based on name
     */
    public UserModel getUserByUsername(RealmModel paramRealmModel, String name) {
        LOG.info("getUserByUsername() paramRealmModel:{}, name:{}", paramRealmModel, name);
        System.out.println("\n getUserByUsername()- using paramRealmModel = "+paramRealmModel+" name = "+name +"\n\n");
        UserModel userModel = null;
        try {
            UserResource user = usersService.getUserByName(name);
            LOG.info("User fetched with name:{} from external service is:{}", name, user);
            System.out.println("\n getUserByUsername()- with name = "+name+" from external service is = "+user+"\n\n");
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error("\n Error fetching user name:{}, from external service is:{} - {} ", name, ex.getMessage(), ex);
            System.out.println("getUserById()- Error fetching user name ="+name+" from external service is ="+ex+"\n\n");
        }
        return userModel;
    }

    public UserModel getUserByEmail(RealmModel paramRealmModel, String paramString) {
        return null;
    }

    public void close() {
        LOG.info("createUserModel()::close()");
        System.out.println("\n createUserModel()::close()\n" );
    }

    private UserModel createUserModel(RealmModel realm, UserResource user) {
        LOG.info("\n\n\n createUserModel() - realm:{} , user:{}", realm, user+"\n\n\n");
        System.out.println("\n createUserModel()- with realm = "+realm+" ,user = "+user +"\n\n");
        UserModel userModel = new UserAdapter(session, realm, model, user);
        LOG.info("\n\n\n Final createUserModel() - userModel:{}", userModel);
        System.out.println("\n createUserModel()- with userModel = "+userModel +"\n\n");
        return userModel;
    }
}
