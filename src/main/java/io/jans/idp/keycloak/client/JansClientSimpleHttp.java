package io.jans.idp.keycloak.client;

import io.jans.idp.keycloak.util.Constants;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.apache.http.impl.client.CloseableHttpClient;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.component.ComponentModel;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.models.KeycloakSession;

import java.util.List;


public class JansClientSimpleHttp {

	private final CloseableHttpClient httpClient;
	private final String baseUrl;
	private final String basicUsername;
	private final String basicPassword;

	public JansClientSimpleHttp(KeycloakSession session, ComponentModel model) {
		this.httpClient = session.getProvider(HttpClientProvider.class).getHttpClient();
		this.baseUrl = model.get(Constants.BASE_URL);
		this.basicUsername = model.get(Constants.CLIENT_ID);
		this.basicPassword = model.get(Constants.PASSWORD);
	}

	


}
