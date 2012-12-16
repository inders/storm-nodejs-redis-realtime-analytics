package storm.starter.bolt;


import java.util.List;


import storm.starter.common.Entry;

import com.google.gson.Gson;

import storm.starter.bolt.RedisBolt.OnDynamicConfigurationListener;

/**
 * 
 * 
 * @author jaydeep
 * 
 */
public class ImobiSentimentBolt extends RedisBolt implements OnDynamicConfigurationListener {
  public static final String CHANNEL = "market";

  public ImobiSentimentBolt() {
    super(CHANNEL);
  }

  @Override
  protected void setupNonSerializableAttributes() {
    // TODO Auto-generated method stub
    super.setupNonSerializableAttributes();
    setupDynamicConfiguration(this);
  }

  @Override
  public boolean publishMessage(String string) {
    Gson gson = new Gson();
    String jsonString = (String) currentTuple.getValue(0);
    if (jsonString == null) {
      return false;
    }

    try {
      Entry entry = gson.fromJson(jsonString, Entry.class);
      System.out.println("Title : " + entry.getTitle() + " Publihsed At : " + entry.getPublishedAt());
      publish(entry.getChannel(), jsonString);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

  @Override
  public void onConfigurationChange(String conf) {
  }

}
