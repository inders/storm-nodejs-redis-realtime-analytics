package storm.starter;

import storm.starter.bolt.LinkFilterBolt;
//import storm.starter.bolt.RedisGooseExtractor;
import storm.starter.bolt.RedisLinksPublisherBolt;
import storm.starter.bolt.RedisMarketBolt;
import storm.starter.bolt.RedisRetweetBolt;
import storm.starter.bolt.RedisTagsPublisherBolt;
import storm.starter.bolt.TwitterFilterBolt;
import storm.starter.bolt.classification.RandomClassificationBolt;
import storm.starter.bolt.persistence.MongoDBPersistenceBolt;
import storm.starter.spout.FeedSpout;
import storm.starter.spout.TwitterSampleSpout;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;

public class GoogleAlertsTopology {

  /**
   * @param args
   */
  public static void main(String[] args) throws Exception {
    
    TopologyBuilder builder = new TopologyBuilder();
    FeedSpout feedSpout = new FeedSpout();
    builder.setSpout("googlealerts", feedSpout);

    // Initial filter

    builder.setBolt("randomclassifier", new RandomClassificationBolt(), 5).shuffleGrouping("googlealerts");
    builder.setBolt("persistence", new MongoDBPersistenceBolt(), 5).shuffleGrouping("randomclassifier");

    Config conf = new Config();
    conf.setDebug(false);

    if (args != null && args.length > 0) {
      conf.setNumWorkers(3);
      StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
    } else {
      LocalCluster cluster = new LocalCluster();
      System.out.println("submit topology to local cluster");
      cluster.submitTopology("googlealerts", conf, builder.createTopology());
    }


  }

}
