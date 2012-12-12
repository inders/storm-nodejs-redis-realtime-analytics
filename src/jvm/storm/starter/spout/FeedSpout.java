/**
 * Copyright [2012] [Datasalt Systems S.L.]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package storm.starter.spout;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import storm.starter.spout.SimpleSpout;
import storm.starter.common.GoogleReader;
import storm.starter.common.XmlParser;
import twitter4j.internal.org.json.JSONException;

/**
 * The feed Spout extends {@link SimpleSpout} and emits Feed URLs to be fetched by {@link FetcherBolt} instances.
 * 
 * @author pere
 * 
 */
@SuppressWarnings("rawtypes")
public class FeedSpout extends SimpleSpout {

  SpoutOutputCollector _collector;
  Queue<Object> feedQueue = new LinkedList<Object>();
  String chkTime;

  public FeedSpout() {
    chkTime = "";
  }

  @Override
  public void nextTuple() {
    Object nextFeed = feedQueue.poll();
    if (nextFeed != null) {
      _collector.emit(new Values(nextFeed), nextFeed);
    } else {
      fetchContent();
//      System.out.println("Sleeping 10 seconds");
      Utils.sleep(10000);
    }
  }

  @Override
  public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
    super.open(conf, context, collector);
    _collector = collector;
    System.out.println("called spout--------------------");
    fetchContent();
    //System.out.println("Sleeping 10 seconds");
    Utils.sleep(10000);
  }

  @Override
  public void ack(Object feedId) {
    // feedQueue.add((String) feedId);
    feedQueue.remove(feedId);
  }

  @Override
  public void fail(Object feedId) {
    // feedQueue.add((String) feedId);
    feedQueue.remove(feedId);
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields("feed"));
  }

  public void fetchContent() {
    try {

      String channel = "GoogleAlerts";
      String[] feeds = XmlParser.parser(GoogleReader.getFeed("inmobibuzz", "inmobi@123"), "", channel);
      for (String feed : feeds) {
        System.out.println(feed);
        feedQueue.add(feed);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
