package storm.starter.common;

public class Constants {

  public static final String CLASS = "class";
  public static final String CONTENT = "content";
  public static final String CHANNEL = "channel";
  public static final String GUID = "id";
  public static final String TITLE = "title";
  public static final String LINK = "link";
  public static final String PUB_DATE = "pub_date";
  public static final String CREATED_TS = "created_on";
  
  public static final String EVENT = "feed";
  public static final String CLASSIFIER_SCRIPT_PATH = "./classifier/classifier.sh";
  
  public enum Classifiers {
    MAXIMUM_ENTROPY,
    NAIVE_BAYES
  }
  
  public enum Channels {
    GOOGLE_ALERTS("googlealerts"), TWITTER("twitter");
    private final String name;

    Channels(String name) {
      this.name = name;
    }
  }

}
