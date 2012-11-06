package com.google.oacurl.engine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.client.OAuthClient;
import net.oauth.http.HttpMessage;

import com.google.oacurl.options.LoginOptions;

public class V1OAuthEngine implements OAuthEngine {
  private static Logger logger = Logger.getLogger(V1OAuthEngine.class.getName());

  @Override
  public String getAuthorizationUrl(OAuthClient client, OAuthAccessor accessor,
      LoginOptions options, String callbackUrl) throws IOException,
      OAuthException, URISyntaxException {
    List<OAuth.Parameter> requestTokenParams = OAuth.newList();
    if (callbackUrl != null) {
      requestTokenParams.add(new OAuth.Parameter(OAuth.OAUTH_CALLBACK, callbackUrl));
    }

    if (options.getScope() != null) {
      requestTokenParams.add(new OAuth.Parameter("scope", options.getScope()));
    }

    if (accessor.consumer.consumerKey.equals("anonymous")) {
      requestTokenParams.add(new OAuth.Parameter("xoauth_displayname", "OACurl"));
    }

    logger.log(Level.INFO, "Fetching request token with parameters: " + requestTokenParams);
    OAuthMessage requestTokenResponse = client.getRequestTokenResponse(accessor, null,
        requestTokenParams);
    logger.log(Level.INFO, "Request token received: " + requestTokenResponse.getParameters());
    logger.log(Level.FINE, requestTokenResponse.getDump().get(HttpMessage.RESPONSE).toString());

    String authorizationUrl = accessor.consumer.serviceProvider.userAuthorizationURL;

    if (options.isBuzz()) {
      authorizationUrl = OAuth.addParameters(authorizationUrl,
          "scope", options.getScope(),
          "domain", accessor.consumer.consumerKey);

      if (accessor.consumer.consumerKey.equals("anonymous")) {
        authorizationUrl = OAuth.addParameters(authorizationUrl,
            "xoauth_displayname", "OACurl");
      }
    }

    if (options.isLatitude()) {
      authorizationUrl = OAuth.addParameters(authorizationUrl,
          "domain", accessor.consumer.consumerKey);
    }

    authorizationUrl = OAuth.addParameters(authorizationUrl, options.getParameters());

    authorizationUrl = OAuth.addParameters(
        authorizationUrl,
        OAuth.OAUTH_TOKEN, accessor.requestToken);
    return authorizationUrl;
  }

  @Override
  public boolean getAccessToken(OAuthAccessor accessor, OAuthClient client,
      String callbackUrl, String verifier) throws IOException, OAuthException, URISyntaxException {
    boolean success;

    List<OAuth.Parameter> accessTokenParams = OAuth.newList(
        OAuth.OAUTH_TOKEN, accessor.requestToken,
        OAuth.OAUTH_VERIFIER, verifier);
    logger.log(Level.INFO, "Fetching access token with parameters: " + accessTokenParams);

    try {
      OAuthMessage accessTokenResponse = client.getAccessToken(accessor, null, accessTokenParams);
      logger.log(Level.INFO, "Access token received: " + accessTokenResponse.getParameters());
      logger.log(Level.FINE, accessTokenResponse.getDump().get(HttpMessage.RESPONSE).toString());

      success = true;
    } catch (OAuthProblemException e) {
      if (e.getHttpStatusCode() == 400) {
        success = false;
      } else {
        throw e;
      }
    }

    return success;
  }

  @Override
  public void authMessage(OAuthAccessor accessor, OAuthMessage message)
      throws OAuthException, IOException, URISyntaxException {
    message.addRequiredParameters(accessor);    
  }
}
