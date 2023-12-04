package factory;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.InsertOneResult;
import model.AlbumInfo;
import model.ImageMetaData;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * The ConnectionManager class is responsible for interacting with a MongoDB database to fetch album information and save new albums.
 * @author ambikakabra
 */
public class ConnectionManager {
  /**
   * Retrieves album information from the database using its unique ID.
   *
   * @param albumId The unique identifier of the album to retrieve.
   * @return An AlbumInfo object representing the album's information, or null if the album is not found.
   * @throws RuntimeException if there is an error while fetching the album.
   */
  public AlbumInfo getAlbumById(String albumId) {
    try {
      ObjectId objectId = new ObjectId(albumId);
      Document albumDoc = ConnectionHelper.albumsCollection
          .find(new Document("_id", objectId))
          .first();
      if (albumDoc != null) {
        String artist = albumDoc.getString("artist");
        String title = albumDoc.getString("title");
        String year = albumDoc.getString("year");
        return new AlbumInfo(albumId, artist, title, year);
      } else {
        return null;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Saves album information and an associated image to the database.
   *
   * @param albumInfo An AlbumInfo object containing information about the album to be saved.
   * @param image The binary image data to be associated with the album.
   * @return An ImageMetaData object containing the unique ID of the saved album and the size of the image.
   */
  public ImageMetaData saveAlbum(AlbumInfo albumInfo, byte[] image) {
    ObjectId id = new ObjectId();
    Document document = new Document("_id", id)
        .append("artist", albumInfo.getArtist())
        .append("title", albumInfo.getTitle())
        .append("year", albumInfo.getYear())
        .append("image", image);
    InsertOneResult result = ConnectionHelper.albumsCollection.insertOne(document);

    return new ImageMetaData(id.toString(), image.length);
  }

  /**
   * Updates the reviews for an album based on the received message parts.
   *
   * <p>This method increments the "likes" field of the album's review document in the MongoDB collection.
   * If the document does not exist, it is created with the specified album ID and likes value.
   *
   * @param messageParts An array containing the album ID at index 0 and the likes value at index 1.
   * @throws NumberFormatException If the likes value in the message parts is not a valid integer.
   */
  public void updateReviews(String[] messageParts) {
    String id = messageParts[0];
    int value = Integer.parseInt(messageParts[1]);
    Document updateDocument = new Document("$inc", new Document("likes", value));
    ConnectionHelper.reviewsCollection.updateOne(new Document("albumID", id), updateDocument, new UpdateOptions().upsert(true));
  }
}