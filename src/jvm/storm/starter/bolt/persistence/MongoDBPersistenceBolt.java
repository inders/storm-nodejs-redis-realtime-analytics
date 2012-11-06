/**
 * 
 */
package storm.starter.bolt.persistence;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import storm.starter.common.Constants;
import storm.starter.common.Constants.Channels;
import storm.starter.common.Entry;

import backtype.storm.tuple.Tuple;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

/**
 * @author suma.shivaprasad
 * 
 */
public class MongoDBPersistenceBolt extends BasePersistenceBolt {

  /*
   * (non-Javadoc)
   * 
   * @see storm.starter.bolt.persistence.BasePersistenceBolt#persist(java.lang.String)
   */

  private final String DB_NAME = "inmobibuzz";
  private Mongo connection;
  private DB db;

  public MongoDBPersistenceBolt() {
  }

  @Override
  public void persist(Tuple tuple) throws PersistenceException {
    String eventJson = (String) tuple.getValueByField(Constants.EVENT);
    Entry channelEvent = Entry.fromJson(eventJson);
    db.requestStart();
    try {
      db.requestEnsureConnection();
      DBCollection colln = db.getCollection(getTableName(channelEvent));
      List<DBObject> docs = getDocuments(channelEvent);
      colln.insert(docs);
    } finally {
      db.requestDone();
    }

  }

  private List<DBObject> getDocuments(Entry channelEvent) {
    ArrayList<DBObject> docs = new ArrayList<DBObject>();
    DBObject doc = new BasicDBObject();
    doc.put(Constants.CHANNEL, channelEvent.getChannel());
    doc.put(Constants.GUID, channelEvent.getId());
    doc.put(Constants.LINK, channelEvent.getLink());
    doc.put(Constants.TITLE, channelEvent.getTitle());
    doc.put(Constants.PUB_DATE, channelEvent.getPublishedAt());
    docs.add(doc);
    return docs;
  }

  @Override
  public void connect() {
    try {
      connection = MongoDriver.getConnection();
      db = connection.getDB(DB_NAME);
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void disconnect() {
    connection.close();
  }
}
