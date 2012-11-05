package storm.starter.bolt.persistence;

import java.util.Map;

import com.google.gson.Gson;

import storm.starter.bolt.classification.SentimentClass;
import storm.starter.common.Constants;
import storm.starter.common.Constants.Channels;
import storm.starter.common.Entry;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;

public abstract class BasePersistenceBolt implements IRichBolt {

  OutputCollector collector;
  @Override
  public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
    this.collector = collector;
    connect();
  }

  @Override
  public void execute(Tuple input) {
    try {
      persist(input);
    } catch (PersistenceException e) {
      e.printStackTrace();
    }
  }
  
  public abstract void connect();
  public abstract void disconnect();
  public abstract void persist(Tuple tuple) throws PersistenceException;

  @Override
  public void cleanup() {
    disconnect();
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
  }
  
  public String getTableName(Entry channelEvent) {
    return channelEvent.getChannel(); 
  }
}
