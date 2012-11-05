/**
 * 
 */
package storm.starter.bolt.persistence;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import storm.starter.utils.Constants;
import storm.starter.utils.Constants.Channels;

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
    db.requestStart();
    try {
      db.requestEnsureConnection();
      DBCollection colln = db.getCollection(tuple.getStringByField(Constants.CHANNEL));
      List<DBObject> docs = getDocuments(tuple);
      colln.insert(docs);
    } finally {
      db.requestDone();
    }

  }

  private List<DBObject> getDocuments(Tuple tuple) {
    ArrayList<DBObject> docs = new ArrayList<DBObject>();
    DBObject doc = new BasicDBObject();
    
    
    doc.put(Constants.CHANNEL, tuple.getStringByField(Constants.CHANNEL));
    doc.put(Constants.GUID, tuple.getStringByField(Constants.GUID));
    doc.put(Constants.LINK, tuple.getStringByField(Constants.LINK));
    doc.put(Constants.TITLE, tuple.getStringByField(Constants.TITLE));
    doc.put(Constants.PUB_DATE, tuple.getStringByField(Constants.PUB_DATE));
    doc.put(Constants.CREATED_TS, tuple.getStringByField(Constants.CREATED_TS));
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
