package storm.starter.bolt.persistence;

import java.util.Map;

import storm.starter.bolt.classification.SentimentClass;
import storm.starter.utils.Constants;
import storm.starter.utils.Constants.Channels;

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

}
