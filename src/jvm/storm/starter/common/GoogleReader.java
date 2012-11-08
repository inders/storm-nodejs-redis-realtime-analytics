package storm.starter.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;




import twitter4j.internal.org.json.JSONException;

public class GoogleReader {
  
  private static final String _AUTHPARAMS = "GoogleLogin auth=";
  private static final String _GOOGLE_LOGIN_URL = "https://www.google.com/accounts/ClientLogin";
  private static final String _READER_BASE_URL = "http://www.google.com/reader/";
  private static final String _API_URL = _READER_BASE_URL + "api/0/";
  private static final String _TOKEN_URL = _API_URL + "token";
  private static final String _USER_INFO_URL = _API_URL + "user-info";
  private static final String _SUBSCRIPTION_LIST_URL = _API_URL + "subscription/list";

  public static String getGoogleAuthKey(String _USERNAME, String _PASSWORD) throws UnsupportedEncodingException, IOException {
    Document doc = Jsoup.connect(_GOOGLE_LOGIN_URL)
        .data("accountType", "GOOGLE", "Email", _USERNAME, "Passwd", _PASSWORD, "service", "reader", "source", "inmobi").userAgent("inmobi")
        .timeout(4000).post();

    // RETRIEVES THE RESPONSE TEXT inc SID and AUTH. We only want the AUTH key.
    String _AUTHKEY = doc.body().text().substring(doc.body().text().indexOf("Auth="), doc.body().text().length());
    _AUTHKEY = _AUTHKEY.replace("Auth=", "");
    return _AUTHKEY;
  }

  public static String getGoogleToken(String _USERNAME, String _PASSWORD) throws UnsupportedEncodingException, IOException {
    Document doc = Jsoup.connect(_TOKEN_URL).header("Authorization", _AUTHPARAMS + getGoogleAuthKey(_USERNAME, _PASSWORD)).userAgent("inmobi")
        .timeout(4000).get();

    // RETRIEVES THE RESPONSE TOKEN
    String _TOKEN = doc.body().text();
    return _TOKEN;
  }

  public static String getUserInfo(String _USERNAME, String _PASSWORD) throws UnsupportedEncodingException, IOException {
    Document doc = Jsoup.connect(_USER_INFO_URL).header("Authorization", _AUTHPARAMS + getGoogleAuthKey(_USERNAME, _PASSWORD))
        .userAgent("inmobi").timeout(4000).get();

    // RETRIEVES THE RESPONSE USERINFO
    String _USERINFO = doc.body().text();
    return _USERINFO;
  }

  public static String getGoogleUserID(String _USERNAME, String _PASSWORD) throws UnsupportedEncodingException, IOException {
    String _USERINFO = getUserInfo(_USERNAME, _PASSWORD);
    String _USERID = (String) _USERINFO.subSequence(11, 31);
    return _USERID;
  }

  public static String getAlertId(String _USERNAME, String _PASSWORD) throws UnsupportedEncodingException, IOException {

    Document doc = Jsoup.connect(_SUBSCRIPTION_LIST_URL).header("Authorization", _AUTHPARAMS + getGoogleAuthKey(_USERNAME, _PASSWORD))
        .userAgent("inmobi").timeout(5000).get();

    for (Element link : doc.select("string")) {
      if (link.attr("name").equals("id")) {
        return link.text().substring(link.text().lastIndexOf("/") + 1);
      }
    }

    return null;
  }

  public static String getFeed(String _USERNAME, String _PASSWORD) throws IOException, JSONException {
    String stringUrl = "http://www.google.com/alerts/feeds/" + getGoogleUserID(_USERNAME, _PASSWORD) + "/" + getAlertId(_USERNAME, _PASSWORD);
    System.out.println(stringUrl);
    URL url = new URL(stringUrl);
    URLConnection connection = url.openConnection();
    connection.addRequestProperty("Referer", "www.google.com");

    String line, xmlString="";
    StringBuilder builder = new StringBuilder();
    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    while ((line = reader.readLine()) != null) {
      xmlString = xmlString + line;
    }
    return xmlString;
  }

}