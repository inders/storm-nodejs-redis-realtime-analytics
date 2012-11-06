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

package com.google.oacurl.options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.oauth.OAuth;
import net.oauth.OAuth.Parameter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;


/**
 * Subclass for {@link Options}s that are used for the login step.
 *
 * @author phopkins@google.com
 */
public class LoginOptions extends CommonOptions {
  private static final Map<String, String> SCOPE_MAP = new HashMap<String, String>();
  static {
    SCOPE_MAP.put("BUZZ", "https://www.googleapis.com/auth/buzz");
    SCOPE_MAP.put("BUZZ_READONLY", "https://www.googleapis.com/auth/buzz.readonly");
    SCOPE_MAP.put("BLOGGER", "http://www.blogger.com/feeds/");
    SCOPE_MAP.put("LATITUDE", "https://www.googleapis.com/auth/latitude.all.best");
    SCOPE_MAP.put("PICASAWEB", "http://picasaweb.google.com/data/");
    SCOPE_MAP.put("PHOTOS", "https://www.googleapis.com/auth/photos");
    SCOPE_MAP.put("CONTACTS", "https://www.google.com/m8/feeds/");
  }

  private String consumerFileName;
  private String consumerKey;
  private String consumerSecret;
  private String serviceProviderFileName;
  private String scope;
  private String browser;
  private boolean nobrowser;
  private boolean noserver;
  private boolean buzz;
  private boolean blogger;
  private boolean latitude;
  private boolean demo;
  private boolean wirelog;
  private List<OAuth.Parameter> parameters;
  private OAuthVersion version;
  private String host;
  private String callback;

  @SuppressWarnings("static-access")
  public LoginOptions() {
    options.addOption(null, "buzz", false, "Use defaults for Buzz");
    options.addOption(null, "blogger", false, "Use defaults for Blogger");
    options.addOption(null, "latitude", false, "Use defaults for Latitude");
    options.addOption("p", "service-provider", true,
        "properties file with service provider URLs (or GOOGLE, YAHOO, TWITTER, etc.)");
    options.addOption("c", "consumer", true, "properties file with consumer key and secret");
    options.addOption(OptionBuilder.withArgName("scope list")
        .withLongOpt("scope")
        .hasArg()
        .withDescription("Scopes (or BUZZ, BUZZ_READONLY, etc.)").create("s"));
    options.addOption("b", "browser", true, "Path to a browser to exec");
    options.addOption(null, "nobrowser", false, "Don't use a browser at all, write URL to stdout");
    options.addOption(null, "noserver", false, "Don't start the server, get token from stdin");
    options.addOption(null, "consumer-key", true, "Consumer key (if file is not specified)");
    options.addOption(null, "consumer-secret", true, "Consumer key (if file is not specified)");
    options.addOption(null, "icon-url", true, "URL to an app icon to show on Buzz page");
    options.addOption(null, "demo", false, "Loads a demo web-app for the login flow");
    options.addOption(null, "host", true, "Sets a host to use besides localhost");
    options.addOption(null, "callback", true, "Use a callback other than the auto-generated localhost one");
    options.addOption(null, "wirelog", false, "Shows HTTP traffic for login requests");
    options.addOption(OptionBuilder.withArgName("query parameter")
        .withLongOpt("param")
        .hasArg()
        .withDescription("Custom parameter to add to the authorization URL").create("P"));
    options.addOption("1", "oauth1.0a", false, "Use OAuth 1.0a (default)");
    options.addOption("2", "oauth2", false, "Use OAuth 2");
    options.addOption(null, "wrap", false, "Use OAuth-WRAP");
  }

  @Override
  public CommandLine parse(String[] args) throws ParseException {
    CommandLine line = super.parse(args);

    parameters = new ArrayList<Parameter>();

    serviceProviderFileName = line.getOptionValue("service-provider");
    consumerFileName = line.getOptionValue("consumer");
    consumerKey = line.getOptionValue("consumer-key");
    consumerSecret = line.getOptionValue("consumer-secret");
    browser = line.getOptionValue("browser");

    // backward compatibility for --icon-url
    if (line.hasOption("icon-url")) {
      parameters.add(new Parameter("iconUrl", line.getOptionValue("icon-url")));
    } else if (line.hasOption("buzz")) {
      parameters.add(new Parameter("iconUrl",
          "http://www.gstatic.com/codesite/ph/images/defaultlogo.png"));
    }

    noserver = line.hasOption("noserver");
    nobrowser = line.hasOption("nobrowser");
    buzz = line.hasOption("buzz");
    blogger = line.hasOption("blogger");
    latitude = line.hasOption("latitude");
    demo = line.hasOption("demo");
    wirelog = line.hasOption("wirelog");
    host = line.getOptionValue("host", "localhost");
    callback = line.getOptionValue("callback", null);

    version = OAuthVersion.V1;
    
    if (line.hasOption("scope")) {
      StringBuilder scopeBuilder = new StringBuilder();
      // Google separates scopes with spaces, Windows Live with commas. Since
      // WL scopes are already short, we use space as the separate in order to
      // support the SCOPE_MAP expansion of Google scope URLs.
      for (String oneScope : line.getOptionValue("scope").split(" ")) {
        if (SCOPE_MAP.containsKey(oneScope)) {
          oneScope = SCOPE_MAP.get(oneScope);
        }

        if (scopeBuilder.length() > 0) {
          scopeBuilder.append(" ");
        }
        scopeBuilder.append(oneScope);
      }

      scope = scopeBuilder.toString();
    } else if (isBuzz()) {
      StringBuilder scopeBuilder = new StringBuilder();
      scopeBuilder.append(SCOPE_MAP.get("BUZZ"));
      scopeBuilder.append(" ");
      scopeBuilder.append(SCOPE_MAP.get("PHOTOS"));
      scope = scopeBuilder.toString();
    } else if (isBlogger()) {
      scope = SCOPE_MAP.get("BLOGGER");
    } else if (isLatitude()) {
      scope = SCOPE_MAP.get("LATITUDE");
      version = OAuthVersion.V2;
    }

    String[] parameterArray = line.getOptionValues("param");
    if (parameterArray != null) {
      for (String param : parameterArray) {
        String[] paramBits = param.split("=", 2);
        parameters.add(new OAuth.Parameter(paramBits[0].trim(), paramBits[1].trim()));
      }
    }

    if (line.hasOption("2")) {
      version = OAuthVersion.V2;
    } else if (line.hasOption("wrap")) {
      version = OAuthVersion.WRAP;
    } else if (line.hasOption("1")) {
      version = OAuthVersion.V1;
    }

    return line;
  }

  public String getServiceProviderFileName() {
    return serviceProviderFileName;
  }

  public String getConsumerFileName() {
    return consumerFileName;
  }

  public String getConsumerKey() {
    return consumerKey;
  }

  public String getConsumerSecret() {
    return consumerSecret;
  }

  public String getScope() {
    return scope;
  }

  public String getBrowser() {
    return browser;
  }

  public boolean isNoBrowser() {
    return nobrowser;
  }

  public boolean isNoServer() {
    return noserver;
  }

  public boolean isBuzz() {
    return buzz;
  }

  public boolean isBlogger() {
    return blogger;
  }

  public boolean isLatitude() {
    return latitude;
  }

  public boolean isDemo() {
    return demo;
  }

  public OAuthVersion getVersion() {
    return version;
  }

  public List<Parameter> getParameters() {
    return parameters;
  }

  public String getHost() {
    return host;
  }

  public String getCallback() {
    return callback;
  }

  public boolean isWirelogVerbose() {
    return wirelog;
  }
}
