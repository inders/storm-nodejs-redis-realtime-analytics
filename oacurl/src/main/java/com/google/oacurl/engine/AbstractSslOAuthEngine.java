package com.google.oacurl.engine;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuth.Parameter;
import net.oauth.OAuthMessage;
import net.oauth.client.OAuthClient;
import net.oauth.http.HttpMessage;
import net.oauth.http.HttpResponseMessage;

/**
 * Common base class for {@link WrapOAuthEngine} and {@link V2OAuthEngine}
 * because the standards are so similar.
 *
 * @author phopkins@twitter.com
 */
public abstract class AbstractSslOAuthEngine implements OAuthEngine {
  private static Logger logger = Logger.getLogger(AbstractSslOAuthEngine.class.getName());

  protected abstract String getClientIdParamName();
  protected abstract String getClientSecretParamName();
  protected abstract String getCallbackUrlParamName();
  protected abstract String getVerificationCodeParamName();
  
  protected abstract String getAuthorizationHeaderPrefix();

  protected abstract String massageCallbackUrlForAccessTokenRequest(
      OAuthAccessor accessor, String callbackUrl) throws IOException;

  protected abstract void parseWrapTokenResponse(String resp, OAuthAccessor accessor);

  protected List<OAuth.Parameter> getAdditionalAccessTokenParams() {
    return new ArrayList<OAuth.Parameter>();
  }

  @Override
  public boolean getAccessToken(OAuthAccessor accessor, OAuthClient client,
      String callbackUrl, String verifier) throws IOException, OAuthException,
      URISyntaxException {
    OAuthConsumer consumer = accessor.consumer;
    
    callbackUrl = massageCallbackUrlForAccessTokenRequest(accessor, callbackUrl);
    
    List<OAuth.Parameter> accessTokenParams = OAuth.newList(
        getClientIdParamName(), consumer.consumerKey,
        getClientSecretParamName(), consumer.consumerSecret,
        getCallbackUrlParamName(), callbackUrl,
        getVerificationCodeParamName(), verifier);

    accessTokenParams.addAll(getAdditionalAccessTokenParams());
    
    logger.log(Level.INFO, "Fetching access token with parameters: " + accessTokenParams);

    String requestString = OAuth.formEncode(accessTokenParams);
    byte[] requestBytes = requestString.getBytes("UTF-8");
    InputStream requestStream = new ByteArrayInputStream(requestBytes);

    String url = consumer.serviceProvider.accessTokenURL;

    HttpMessage request = new HttpMessage("POST", new URL(url), requestStream);
    request.headers.add(new Parameter("Content-Type", "application/x-www-form-urlencoded"));
    request.headers.add(new Parameter("Content-Length", "" + requestString.length()));

    HttpResponseMessage response = client.getHttpClient().execute(request,
        client.getHttpParameters());
    InputStream bodyStream = response.getBody();
    BufferedReader reader = new BufferedReader(new InputStreamReader(bodyStream));

    StringBuilder respBuf = new StringBuilder();

    String line;
    while ((line = reader.readLine()) != null) {
      respBuf.append(line);
    }

    parseWrapTokenResponse(respBuf.toString(), accessor);
    
    return accessor.accessToken != null;
  }

  @Override
  public void authMessage(OAuthAccessor accessor, OAuthMessage message)
      throws OAuthException, IOException, URISyntaxException {
    // By not calling #addRequiredParameters, we prevent all the OAuth 1.0
    // signing bits and adding of headers, while retaining all of our existing
    // code around HttpMessages and such for OAuth-WRAP / V2.
    message.getHeaders().add(new OAuth.Parameter(
        "Authorization",
        getAuthorizationHeaderPrefix() + accessor.accessToken));
  }
}
