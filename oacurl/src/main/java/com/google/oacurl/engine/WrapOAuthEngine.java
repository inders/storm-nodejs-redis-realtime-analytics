package com.google.oacurl.engine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.oauth.OAuth;
import net.oauth.OAuth.Parameter;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.client.OAuthClient;

import com.google.oacurl.options.LoginOptions;

public class WrapOAuthEngine extends AbstractSslOAuthEngine {
  private static Logger logger = Logger.getLogger(WrapOAuthEngine.class.getName());

  @Override
  public String getAuthorizationUrl(OAuthClient client, OAuthAccessor accessor,
      LoginOptions options, String callbackUrl) throws IOException,
      OAuthException, URISyntaxException {
    OAuthConsumer consumer = accessor.consumer;

    // This isn't used for anything fancy or cryptographic. Instead it's a
    // demonstration of best practice that the callback URL for OAuth-WRAP
    // should be unique for the request. Basically, XSRF protection.
    // We just use requestToken because it's handy and not use by OAuth-WRAP.
    String requestToken = Long.toHexString(new Random().nextLong());

    accessor.requestToken = requestToken;

    List<Parameter> authParams = new ArrayList<Parameter>();
    authParams.add(new OAuth.Parameter("wrap_client_id", consumer.consumerKey));

    if (callbackUrl != null) {
      authParams.add(new OAuth.Parameter("wrap_callback",
          addRequestTokenToCallbackUrl(accessor, callbackUrl)));
    }

    if (options.getScope() != null) {
      authParams.add(new OAuth.Parameter("wrap_scope", options.getScope()));
    }

    return OAuth.addParameters(consumer.serviceProvider.userAuthorizationURL,
        authParams);
  }

  /**
   * The callback needs to be exactly the same, so put in
   * the "requestToken" originally generated in
   * {@link #getAuthorizationUrl(OAuthClient, OAuthAccessor, LoginOptions, String)}
   */
  @Override
  protected String massageCallbackUrlForAccessTokenRequest(
    OAuthAccessor accessor, String callbackUrl) throws IOException {
    if (callbackUrl != null) {
      return addRequestTokenToCallbackUrl(accessor, callbackUrl);
    } else {
      return "";
    }
  }

  private String addRequestTokenToCallbackUrl(OAuthAccessor accessor,
      String callbackUrl) throws IOException {
    return OAuth.addParameters(callbackUrl, OAuth.OAUTH_TOKEN, accessor.requestToken);
  }

  @Override
  protected String getClientIdParamName() {
    return "wrap_client_id";
  }

  @Override
  protected String getClientSecretParamName() {
    return "wrap_client_secret";
  }

  @Override
  protected String getCallbackUrlParamName() {
    return "wrap_callback";
  }

  @Override
  protected String getVerificationCodeParamName() {
    return "wrap_verification_code";
  }

  @Override
  protected String getAuthorizationHeaderPrefix() {
    return "WRAP access_token=";
  }

  @Override
  protected void parseWrapTokenResponse(String resp, OAuthAccessor accessor) {
    List<Parameter> params = OAuth.decodeForm(resp);

    List<String> logList = new ArrayList<String>();

    for (Parameter param : params) {
      logList.add(param.getKey() + "=" + param.getValue());

      if (param.getKey().equals("wrap_access_token")) {
        accessor.accessToken = param.getValue();
        accessor.tokenSecret = "";
      }
    }

    logger.log(Level.INFO, "Access token response params: " + logList);    
  }
}
