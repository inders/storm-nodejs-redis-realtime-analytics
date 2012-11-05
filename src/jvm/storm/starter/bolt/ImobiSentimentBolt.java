package storm.starter.bolt;


import java.util.List;


import storm.starter.common.Entry;

import com.google.gson.Gson;

import storm.starter.bolt.RedisBolt.OnDynamicConfigurationListener;

/**
 * 
 * This bolt will get an url coming from the android market and will try to get the html and parse the information, publishing into a redis
 * channel the information recollected.
 * 
 * @author arcturus@ardeenelinfierno.com
 * 
 */
public class ImobiSentimentBolt extends RedisBolt implements OnDynamicConfigurationListener {

  @Override
  protected void setupNonSerializableAttributes() {
    // TODO Auto-generated method stub
    super.setupNonSerializableAttributes();
    setupDynamicConfiguration(this);
  }

  @Override
  public List<Object> publishMessage(String string) {
    Gson gson = new Gson();
    String jsonString = (String) currentTuple.getValue(0);
    if (jsonString == null) {
      return null;
    }

    try {
      Entry entry = gson.fromJson(jsonString, Entry.class);
      publish(jsonString, entry.getChannel());
    } catch (Exception e) {
      return null;
    }
    return null;
  }

  @Override
  public void onConfigurationChange(String conf) {
  }

}
