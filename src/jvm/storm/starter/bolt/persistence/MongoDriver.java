package storm.starter.bolt.persistence;

import java.net.UnknownHostException;

import com.mongodb.Mongo;
import com.mongodb.WriteConcern;

public class MongoDriver {

  public static Mongo getConnection() throws UnknownHostException {

    Mongo connection = null;
    connection = new Mongo("localhost", 27017);
    connection.setWriteConcern(WriteConcern.SAFE);
    return connection;

  }
}
