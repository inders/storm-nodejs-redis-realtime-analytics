package storm.starter.bolt.classification;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;

import storm.starter.common.Constants;
import storm.starter.common.Constants.Classifiers;

import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

/**
 * Bolt that will emit those twitter status that have urls on it.
 * 
 * @author arcturus@ardeenelinfierno.com
 * 
 */
public class MaxEntClassificationBolt extends BaseClassificationBolt {

  @Override
  public SentimentClass classify(String channel, String input) {
    String classficationResult = getClassificationOutput(Constants.Classifiers.MAXIMUM_ENTROPY, input);
    if (classficationResult != null) {
      return SentimentClass.valueOf(classficationResult);
    }
    return SentimentClass.neutral;
  }

  private String getClassificationOutput(Classifiers maximumEntropy, String input) {
    String result = "";
    input = StringUtils.strip(input, "'");
    String cmd = "sh " + Constants.CLASSIFIER_SCRIPT_PATH + " '" + input + "'";
    Process p;
    try {
      System.out.println("cmd : " + cmd);
      Runtime rt = Runtime.getRuntime();
      p = rt.exec(cmd);
      p.waitFor();

      BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
      result = reader.readLine();
      System.out.println("Class : " + result);
      if (result != null) {
        String[] resultArr = result.split(" ");
        System.out.println("Score : " + resultArr[1]);
        return resultArr[0];
      } else {
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        while (errorReader.readLine() != null) {
          System.out.println("ERROR : " + errorReader.readLine());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return result;
  }

  public static void main(String[] args) {
    System.out.println("Classification result : " + new MaxEntClassificationBolt().classify(Constants.Channels.GOOGLE_ALERTS.name(), args[0]));
  }
}
