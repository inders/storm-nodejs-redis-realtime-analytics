package storm.starter.bolt.classification;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

/**
 * Bolt that will emit those twitter status that have urls on it.
 * 
 * @author arcturus@ardeenelinfierno.com
 * 
 */
public class RandomClassificationBolt extends BaseClassificationBolt {

  private static Random randClassifier = new Random();

  @Override
  public SentimentClass classify(String channel, String input) {
    switch (randClassifier.nextInt(2)) {
    case 0:
      return SentimentClass.POSITIVE;
    case 1:
      return SentimentClass.NEGATIVE;
    default:
      return SentimentClass.NEUTRAL;
    }
  }
}
