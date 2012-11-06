package com.google.oacurl.engine;

import java.io.IOException;
import java.net.URISyntaxException;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.client.OAuthClient;

import com.google.oacurl.options.LoginOptions;

public interface OAuthEngine {
  String getAuthorizationUrl(
      OAuthClient client,
      OAuthAccessor accessor,
      LoginOptions options,
      String callbackUrl) throws IOException, OAuthException, URISyntaxException;

  boolean getAccessToken(
      OAuthAccessor accessor,
      OAuthClient client,
      String callbackUrl,
      String verifier) throws IOException, OAuthException, URISyntaxException;

  void authMessage(
      OAuthAccessor accessor,
      OAuthMessage message) throws OAuthException, IOException,  URISyntaxException;
}
