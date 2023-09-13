package io.jans.idp.keycloak.client;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
public interface JansTokenClient {

    @POST
    @Path("/token")
    @Produces({MediaType.APPLICATION_JSON})
    Response requestAccessToken(
            @FormParam("grant_type")
                    String grantType,
            @FormParam("code")
                    String code,
            @FormParam("redirect_uri")
                    String redirectUri,
            @FormParam("username")
                    String username,
            @FormParam("password")
                    String password,
            @FormParam("scope")
                    String scope,
            @FormParam("assertion")
                    String assertion,
            @FormParam("refresh_token")
                    String refreshToken,
            @FormParam("client_id")
                    String clientId,
            @FormParam("client_secret")
                    String clientSecret,
            @FormParam("code_verifier")
                    String codeVerifier,
            @FormParam("ticket")
                    String ticket,
            @FormParam("claim_token")
                    String claimToken,
            @FormParam("claim_token_format")
                    String claimTokenFormat,
            @FormParam("pct")
                    String pctCode,
            @FormParam("rpt")
                    String rptCode,
            @FormParam("auth_req_id")
                    String authReqId,
            @FormParam("device_code")
                    String deviceCode,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response,
            @Context SecurityContext sec);
}
