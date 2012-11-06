// Copyright 2010 Google, Inc. All rights reserved.
// Copyright 2009 John Kristian.
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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

import com.google.oacurl.options.LoginOptions;

/**
 * Class that runs a Jetty server on a free port, waiting for OAuth to redirect
 * to it with the one-time authorization token.
 * <p>
 * Initially derived from the oauth-example-desktop by John Kristian.
 *
 * @author phopkins@google.com
 */
public class LoginCallbackServer {
  public enum TokenStatus {
    MISSING,
    VALID,
    INVALID
  }
 
  private static final String DEMO_PATH = "/";
  private static final String CALLBACK_PATH = "/OAuthCallback";

  private final LoginOptions options;

  private int port;
  private String host;
  private Server server;

  private TokenStatus tokenStatus = TokenStatus.MISSING;
  private String authorizationUrl;

  private Map<String, String> verifierMap = new HashMap<String, String>();

  public LoginCallbackServer(LoginOptions options) {
    this.options = options;
  }

  public void start() {
    if (server != null) {
      throw new IllegalStateException("Server is already started");
    }

    try {
      port = getUnusedPort();
      host = options.getHost();
      server = new Server(port);

      for (Connector c : server.getConnectors()) {
        c.setHost(host);
      }

      server.addHandler(new CallbackHandler());
      server.addHandler(new DemoHandler());

      server.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void stop() throws Exception {
    if (server != null) {
      server.stop();
      server = null;
    }
  }

  public void setTokenStatus(TokenStatus tokenStatus) {
    this.tokenStatus = tokenStatus;
  }

  public void setAuthorizationUrl(String authorizationUrl) {
    this.authorizationUrl = authorizationUrl;
  }
 
  public String getDemoUrl() throws IOException {
    if (port == 0) {
      throw new IllegalStateException("Server is not yet started");
    }

    return "http://" + host + ":" + port + DEMO_PATH;
  }
 
  public String getCallbackUrl() {
    if (port == 0) {
      throw new IllegalStateException("Server is not yet started");
    }

    return "http://" + host + ":" + port + CALLBACK_PATH;
  }

  private static int getUnusedPort() throws IOException {
    Socket s = new Socket();
    s.bind(null);

    try {
      return s.getLocalPort();
    } finally {
      s.close();
    }
  }

  /**
   * Call that blocks until the OAuth provider redirects back here with the
   * verifier token.
   *
   * @param accessor Accessor whose request token we're waiting for a verifier
   *     token for.
   * @param waitMillis Amount of time we're willing to wait, it millis.
   * @return The verifier token, or null if there was a timeout.
   */
  public String waitForVerifier(OAuthAccessor accessor, long waitMillis) {
    long startTime = System.currentTimeMillis();

    synchronized (verifierMap) {
      while (!verifierMap.containsKey(accessor.requestToken)) {
        try {
          verifierMap.wait(3000);
        } catch (InterruptedException e) {
          return null;
        }

        if (waitMillis != -1 && System.currentTimeMillis() > startTime + waitMillis) {
          return null;
        }
      }

      return verifierMap.remove(accessor.requestToken);
    }
  }

  /**
   * Jetty handler that takes the verifier token passed over from the OAuth
   * provider and stashes it where
   * {@link LoginCallbackServer#waitForVerifier} will find it.
   */
  public class CallbackHandler extends AbstractHandler {
    public void handle(String target, HttpServletRequest request,
        HttpServletResponse response, int dispatch)
        throws IOException, ServletException {
      if (!CALLBACK_PATH.equals(target)) {
        return;
      }

      String requestTokenName;
      String verifierName;
      switch (options.getVersion()) {
      case V1:
        verifierName = OAuth.OAUTH_VERIFIER;
        requestTokenName = OAuth.OAUTH_TOKEN;
        break;
      case V2:
        verifierName = "code";
        requestTokenName = "state";
        break;
      case WRAP:
        verifierName = "wrap_verification_code";
        requestTokenName = OAuth.OAUTH_TOKEN;
        break;
      default:
        throw new AssertionError("Unknown version: " + options.getVersion());
      }

      String verifier = request.getParameter(verifierName);
      String requestToken = request.getParameter(requestTokenName);

      if (verifier != null) {
        writeLandingHtml(response);
  
        synchronized (verifierMap) {
          verifierMap.put(requestToken, verifier);
          verifierMap.notifyAll();
        }
      } else {
        writeErrorHtml(request, response);
      }

      response.flushBuffer();
      ((Request) request).setHandled(true);
    }

    private void writeLandingHtml(HttpServletResponse response) throws IOException {
      response.setStatus(HttpServletResponse.SC_OK);
      response.setContentType("text/html");

      PrintWriter doc = response.getWriter();
      doc.println("<html>");
      doc.println("<head><title>OAuth Authentication Token Recieved</title></head>");
      doc.println("<body>");
      doc.println("Received verifier token. Closing...");
      doc.println("<script type='text/javascript'>");
      // We open "" in the same window to trigger JS ownership of it, which lets
      // us then close it via JS, at least in Chrome.
      doc.println("window.setTimeout(function() {");
      doc.println("    window.open('', '_self', ''); window.close(); }, 1000);");
      doc.println("if (window.opener) { window.opener.checkToken(); }");
      doc.println("</script>");
      doc.println("</body>");
      doc.println("</HTML>");
      doc.flush();
    }

    private void writeErrorHtml(HttpServletRequest request,
        HttpServletResponse response) throws IOException {
      response.setStatus(HttpServletResponse.SC_OK);
      response.setContentType("text/html");

      PrintWriter doc = response.getWriter();
      doc.println("<html>");
      doc.println("<head><title>OAuth Authentication Token Not Recieved</title></head>");
      doc.println("<body>");
      doc.println("Did not receive verifier token. One of these parameters might be interesting:");
      doc.println("<dl>");

      @SuppressWarnings("unchecked")
      Map<String, String[]> parameterMap = request.getParameterMap();
      for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
        doc.println("<dt>" + param.getKey() + "</dt>");
        for (String value : param.getValue()) {
          doc.println("<dd>" + value + "</dd>");
        }
      }

      doc.println("</dl>");
      doc.println("</body>");
      doc.println("</HTML>");
      doc.flush();
    }
}

  /**
   * Jetty handler that takes the verifier token passed over from the OAuth
   * provider and stashes it where
   * {@link LoginCallbackServer#waitForVerifier} will find it.
   */
  public class DemoHandler extends AbstractHandler {
    public void handle(String target, HttpServletRequest request,
        HttpServletResponse response, int dispatch)
        throws IOException, ServletException {
      if (!DEMO_PATH.equals(target)) {
        return;
      }

      writeDemoHtml(LoginCallbackServer.this.authorizationUrl, response);
    }

    private void writeDemoHtml(String authorizationUrl,
        HttpServletResponse response) throws IOException {
      response.setStatus(HttpServletResponse.SC_OK);
      response.setContentType("text/html");

      PrintWriter doc = response.getWriter();
      doc.println("<html>");
      doc.println("<head><title>OACurl Demo App</title></head>");
      doc.println("<body>");
      doc.println("<script type='text/javascript'>");
      doc.println("function launchAuth() {");
      doc.println("  window.open('" + authorizationUrl + "', 'oauth', ");
      doc.println("      'width=640,height=450,toolbar=no,location=yes');");
      doc.println("}");
      doc.println("function checkToken() {");
      // 1s delay for reload because we want to wait for the OAuth check to
      // happen in the background. One would presumably make a nicer flow-of-
      // control in a real app.
      doc.println("  window.setTimeout(function() { window.location.reload(); }, 1000);");
      doc.println("}");
      doc.println("</script>");
      doc.println("<h1>OACurl Demo App</h1>");
      doc.println("<p>Current token status: <b>" + LoginCallbackServer.this.tokenStatus + "</b></p>");
      doc.println("<button onclick='launchAuth()'>OAuth Login</button><br />");
      doc.println("<h2>Recommended JavaScript for Authorization</h2>");
      doc.println("<pre>");
      doc.println("window.open('<i>http://...</i>',");
      doc.println("    'oauth', ");
      doc.println("    'width=640,height=450,toolbar=no,location=yes');");
      doc.println("</pre>");
      doc.println("</body>");
      doc.println("</HTML>");
      doc.flush();
    }
  }
}
