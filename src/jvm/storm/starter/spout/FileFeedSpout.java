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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import com.google.gson.Gson;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import storm.starter.spout.SimpleSpout;
import storm.starter.common.Entry;
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
public class FileFeedSpout extends SimpleSpout {

  SpoutOutputCollector _collector;
  Queue<Object> feedQueue = new LinkedList<Object>();
  String chkTime;
  BufferedReader br = null;

	public FileFeedSpout() {
		chkTime="";
	}

	@Override
	public void nextTuple() {
		Object nextFeed = feedQueue.poll();
    if(nextFeed != null) {
      _collector.emit(new Values(nextFeed), nextFeed);
    }
	}

	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector){
		super.open(conf, context, collector);
    _collector = collector;
    System.out.println("called spout--------------------");
//    Utils.sleep(3600000/60*30);
    try{
     
      String channel = "GoogleAlerts";
      String sCurrentLine;
      
      File inFile = new File("feed.txt");
      br = new BufferedReader(new FileReader(inFile));
     
      ArrayList<String> listJson = new ArrayList<String>();
      while ((sCurrentLine = br.readLine()) != null) {
        String[] sCurrentLineArray = sCurrentLine.split("\u0001");
        Entry entry = new Entry();
        Gson gson = new Gson();
        entry.setTitle(sCurrentLineArray[0]);
        entry.setPublishedAt(sCurrentLineArray[1]);
        entry.setContent(sCurrentLineArray[2]);
        entry.setLink(sCurrentLineArray[3]);
        entry.setAuthor(sCurrentLineArray[4]);
        String entryJson = gson.toJson(entry);
        listJson.add(entryJson);
        System.out.println(entryJson);
      }
      String[] jsonString = new String[listJson.size()];
      listJson.toArray( jsonString );
      
				for(String feed: listJson) {
				  System.out.println(feed);
					feedQueue.add(feed);
				}
			
    }catch(Exception e)
    {
    	e.printStackTrace();
    }
	}
	
	@Override
	public void ack(Object feedId) {
		feedQueue.add((String) feedId);
	}

	@Override
	public void fail(Object feedId) {
		feedQueue.add((String) feedId);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("feed"));
	}
}
