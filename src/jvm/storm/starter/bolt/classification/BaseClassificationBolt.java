package storm.starter.bolt.classification;

import java.util.Map;

import com.google.gson.Gson;

import storm.starter.common.Entry;
import storm.starter.common.Constants;
import storm.starter.utils.Utils;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

/**
 * Bolt that will emit those twitter status that have urls on it.
 * 
 * @author arcturus@ardeenelinfierno.com
 * 
 */
public abstract class BaseClassificationBolt implements IRichBolt {

  OutputCollector collector;

  @Override
  public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
    this.collector = collector;
  }

  @Override
  public void execute(Tuple tuple) {

    String eventJson = (String) tuple.getValueByField(Constants.EVENT);
    Gson gson = new Gson();
    Entry channelEvent = gson.fromJson(eventJson, Entry.class);
    String preProcessedContent = preProcessContent(channelEvent.getContent());
    SentimentClass classificationResult = classify(channelEvent.getChannel(), preProcessedContent);
    channelEvent.setSentiment(classificationResult);
    collector.emit(tuple, new Values(gson.toJson(channelEvent)));

    // TODO - Think of cases where it should ack/fail
//    if (channelEvent != null) {
      collector.ack(tuple);
//    } else {
//      collector.fail(tuple);
//    }
  }

  public abstract SentimentClass classify(String channel, String input);

  @Override
  public void cleanup() {
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields(Constants.EVENT));
  }

  protected String preProcessContent(String input) {
    return Utils.html2text(input);
  }

}
