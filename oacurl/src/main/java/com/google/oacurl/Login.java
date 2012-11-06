// Copyright 2010 Google, Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.oacurl;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthProblemException;
import net.oauth.OAuthServiceProvider;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;

import com.google.oacurl.LoginCallbackServer.TokenStatus;
import com.google.oacurl.dao.AccessorDao;
import com.google.oacurl.dao.ConsumerDao;
import com.google.oacurl.dao.ServiceProviderDao;
import com.google.oacurl.engine.OAuthEngine;
import com.google.oacurl.engine.V1OAuthEngine;
import com.google.oacurl.engine.V2OAuthEngine;
import com.google.oacurl.engine.WrapOAuthEngine;
import com.google.oacurl.options.LoginOptions;
import com.google.oacurl.options.OAuthVersion;
import com.google.oacurl.util.LoggingConfig;
import com.google.oacurl.util.OAuthUtil;
import com.google.oacurl.util.PropertiesProvider;

/**
 * Main class for doing the initial OAuth dance to get an access token and
 * secret.
 *
 * @author phopkins@google.com
 */
public class Login {

  private static Logger logger = Logger.getLogger(Login.class.getName());

  public static void main(String[] args) throws Exception {
    LoginOptions options = new LoginOptions();
    try {
      options.parse(args);
    } catch (ParseException e) {
      System.err.println(e.getMessage());
      System.exit(-1);
    }

    if (options.isHelp()) {
      new HelpFormatter().printHelp(" ", options.getOptions());
      System.exit(0);
    }

    if (options.isInsecure()) {
      SSLSocketFactory.getSocketFactory().setHostnameVerifier(new AllowAllHostnameVerifier());
    }

    LoggingConfig.init(options.isVerbose());
    if (options.isWirelogVerbose()) {
      LoggingConfig.enableWireLog();
    }

    ServiceProviderDao serviceProviderDao = new ServiceProviderDao();
    ConsumerDao consumerDao = new ConsumerDao(options);
    AccessorDao accessorDao = new AccessorDao();

    String serviceProviderFileName = options.getServiceProviderFileName();
    if (serviceProviderFileName == null) {
      if (options.isBuzz()) {
        // Buzz has its own provider because it has a custom authorization URL
        serviceProviderFileName = "BUZZ";
      } else if (options.getVersion() == OAuthVersion.V2) {
        serviceProviderFileName = "GOOGLE_V2";
      } else {
        serviceProviderFileName = "GOOGLE";
      }
    }

    // We have a wee library of service provider properties files bundled into
    // the resources, so we set up the PropertiesProvider to search for them
    // if the file cannot be found.
    OAuthServiceProvider serviceProvider = serviceProviderDao.loadServiceProvider(
        new PropertiesProvider(serviceProviderFileName,
            ServiceProviderDao.class, "services/").get());
    OAuthConsumer consumer = consumerDao.loadConsumer(
        new PropertiesProvider(options.getConsumerFileName()).get(), serviceProvider);
    OAuthAccessor accessor = accessorDao.newAccessor(consumer);

    OAuthClient client = new OAuthClient(new HttpClient4());

    LoginCallbackServer callbackServer = null;

    boolean launchedBrowser = false;

    try {
      if (!options.isNoServer()) {
        callbackServer = new LoginCallbackServer(options);
        callbackServer.start();        
      }

      String callbackUrl;
      if (options.getCallback() != null) {
        callbackUrl = options.getCallback();
      } else if (callbackServer != null) {
        callbackUrl = callbackServer.getCallbackUrl();
      } else {
        callbackUrl = null;
      }

      OAuthEngine engine;
      switch (options.getVersion()) {
      case V1:
        engine = new V1OAuthEngine();
        break;
      case V2:
        engine = new V2OAuthEngine();
        break;
      case WRAP:
        engine = new WrapOAuthEngine();
        break;
      default:
        throw new IllegalArgumentException("Unknown version: " + options.getVersion());
      }

      do {
        String authorizationUrl = engine.getAuthorizationUrl(client, accessor, options, callbackUrl);

        if (!options.isNoServer()) {
          callbackServer.setAuthorizationUrl(authorizationUrl);
        }

        if (!launchedBrowser) {
          String url = options.isDemo() ? callbackServer.getDemoUrl() : authorizationUrl;
    
          if (options.isNoBrowser()) {
            System.out.println(url);
            System.out.flush();
          } else {
            launchBrowser(options, url);        
          }
  
          launchedBrowser = true;
        }
 
        accessor.accessToken = null;

        logger.log(Level.INFO, "Waiting for verification token...");
        String verifier;
        if (options.isNoServer()) {
          System.out.print("Verification token: ");
          BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
          verifier = "";
          while (verifier.isEmpty()) {
            String line = reader.readLine();
            if (line == null) {
              System.exit(-1);
            }
            verifier = line.trim();
          }
        } else {
          verifier = callbackServer.waitForVerifier(accessor, -1);
          if (verifier == null) {
            System.err.println("Wait for verifier interrupted");
            System.exit(-1);
          }        
        }
        logger.log(Level.INFO, "Verification token received: " + verifier);

        boolean success = engine.getAccessToken(accessor, client, callbackUrl, verifier);

        if (success) {
          if (callbackServer != null) {
            callbackServer.setTokenStatus(TokenStatus.VALID);
          }

          Properties loginProperties = new Properties();
          accessorDao.saveAccessor(accessor, loginProperties);
          consumerDao.saveConsumer(consumer, loginProperties);
          loginProperties.put("oauthVersion", options.getVersion().toString());
          new PropertiesProvider(options.getLoginFileName()).overwrite(loginProperties);
        } else {
          if (callbackServer != null) {
            callbackServer.setTokenStatus(TokenStatus.INVALID);
          }
        }
      } while (options.isDemo());
    } catch (OAuthProblemException e) {
      OAuthUtil.printOAuthProblemException(e);
    } finally {
      if (callbackServer != null) {
        callbackServer.stop();
      }
    }
  }

  private static void launchBrowser(LoginOptions options,
      String authorizationUrl) {
    logger.log(Level.INFO, "Redirecting to URL: " + authorizationUrl);

    boolean browsed = false;
    if (options.getBrowser() == null) {
      if (Desktop.isDesktopSupported()) {
        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Action.BROWSE)) {
          try {
            desktop.browse(URI.create(authorizationUrl));
            browsed = true;
          } catch (IOException e) {
            // In some situations "BROWSE" appears supported but throws an
            // exception.
            logger.log(Level.WARNING, "Error opening browser for Desktop#browse(String)",
                options.isVerbose() ? e : null);
          }
        } else {
          logger.log(Level.WARNING, "java.awt.Desktop BROWSE action not supported.");
        }
      } else {
        logger.log(Level.WARNING, "java.awt.Desktop not supported. You should use Java 1.6.");
      }
    }

    if (!browsed) {
      String browser = options.getBrowser();
      if (browser == null) {
        browser = "google-chrome";
      }

      try {
        Runtime.getRuntime().exec(new String[] { browser, authorizationUrl });
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error running browser: " + browser + ". " +
            "Specify a browser with --browser or use --nobrowser to print URL.",
            options.isVerbose() ? e : null);
        System.exit(-1);
      }
    }
  }
}
