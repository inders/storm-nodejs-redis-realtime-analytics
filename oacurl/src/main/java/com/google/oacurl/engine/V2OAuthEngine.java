package com.google.oacurl.engine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import net.oauth.OAuth;
import net.oauth.OAuth.Parameter;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.client.OAuthClient;

import com.google.oacurl.options.LoginOptions;

public class V2OAuthEngine extends AbstractSslOAuthEngine {
  private static final String OOB_CALLBACK_URL = "urn:ietf:wg:oauth:2.0:oob";

  private static Logger logger = Logger.getLogger(V2OAuthEngine.class.getName());

  @Override
  public String getAuthorizationUrl(OAuthClient client, OAuthAccessor accessor,
      LoginOptions options, String callbackUrl) throws IOException,
      OAuthException, URISyntaxException {
    OAuthConsumer consumer = accessor.consumer;

    // We want something uniquely identifying in the callback URL to prevent
    // hijacking / forced authentication stuff. We generate a random string
    // and then pass it in the "state" parameter of the URL. The OAuth server
    // (or, at least, Google's implementation) will add this state parameter
    // on to the callback URI when it redirects the user.
    String requestToken = Long.toHexString(new Random().nextLong());
    accessor.requestToken = requestToken;

    List<Parameter> authParams = new ArrayList<Parameter>();
    authParams.add(new OAuth.Parameter("client_id", consumer.consumerKey));
    authParams.add(new OAuth.Parameter("state", requestToken));

    if (callbackUrl == null) {
      callbackUrl = OOB_CALLBACK_URL;
    }
    
    authParams.add(new OAuth.Parameter("redirect_uri", callbackUrl));
    authParams.add(new OAuth.Parameter("response_type", "code"));

    if (options.getScope() != null) {
      authParams.add(new OAuth.Parameter("scope", options.getScope()));
    }

    return OAuth.addParameters(consumer.serviceProvider.userAuthorizationURL,
        authParams);
  }

  @Override
  protected String getClientIdParamName() {
    return "client_id";
  }

  @Override
  protected String getClientSecretParamName() {
    return "client_secret";
  }

  @Override
  protected String getCallbackUrlParamName() {
    return "redirect_uri";
  }

  @Override
  protected String getVerificationCodeParamName() {
    return "code";
  }

  @Override
  protected String getAuthorizationHeaderPrefix() {
    return "Bearer ";
  }

  @Override
  protected String massageCallbackUrlForAccessTokenRequest(
      OAuthAccessor accessor, String callbackUrl) throws IOException {
    if (callbackUrl != null) {
      return callbackUrl;
    } else {
      return OOB_CALLBACK_URL;
    }
  }

  @Override
  protected List<Parameter> getAdditionalAccessTokenParams() {
    return OAuth.newList("grant_type", "authorization_code");
  }

  @Override
  protected void parseWrapTokenResponse(String resp, OAuthAccessor accessor) {
    JSONObject respObj = (JSONObject) JSONValue.parse(resp);
    
    String accessToken = (String) respObj.get("access_token");
    if (accessToken != null) {
      accessor.accessToken = accessToken;
      accessor.tokenSecret = "";
    }

    logger.log(Level.INFO, "Access token response: " + resp);
  }
}
