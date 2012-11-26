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

  private static Random randClassifier = new Random(System.currentTimeMillis());

  @Override
  public SentimentClass classify(String channel, String input) {
    switch (randClassifier.nextInt(2)) {
    case 0:
      return SentimentClass.pos;
    case 1:
      return SentimentClass.neg;
    default:
      return SentimentClass.neutral;
    }
  }
  
  public static void main(String[] args) {
    System.out.println(new RandomClassificationBolt().classify("ga", "123").toString());
    System.out.println(new RandomClassificationBolt().classify("ga", "123").toString());
    System.out.println(new RandomClassificationBolt().classify("ga", "123").toString());
  }
}
