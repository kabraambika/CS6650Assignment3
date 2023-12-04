package factory;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * The ConnectionHelper class is responsible for establishing a connection to a MongoDB database and providing access to collections.
 * @author ambikakabra
 */
public class ConnectionHelper {
  private static String connectionString = "mongodb://localhost:27017/?directConnection=true&serverSelectionTimeoutMS=2000&appName=mongosh+2.0.2";
  private static MongoClient mongoClient = MongoClients.create(connectionString);
  private static MongoDatabase sampleTrainingDB = mongoClient.getDatabase("AlbumStore");

  /**
   * A reference to the MongoDB collection named "albums" in the "AlbumStore" database.
   */
  static MongoCollection<Document> albumsCollection = sampleTrainingDB.getCollection("albums");

  /**
   * A reference to the MongoDB collection named "reviews" in the "AlbumStore" database.
   */
  static MongoCollection<Document> reviewsCollection = sampleTrainingDB.getCollection("reviews");
}
