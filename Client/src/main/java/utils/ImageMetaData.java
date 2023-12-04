package utils;

/**
 * The ImageMetaData class represents metadata information for an image, including album ID and image size.
 * @author ambikakabra
 */
public class ImageMetaData {
  private String albumID;
  private long imageSize;

  /**
   * Constructs an ImageMetaData object with the specified album ID and image size.
   *
   * @param albumID The unique identifier of the album to which the image belongs.
   * @param imageSize The size of the image in bytes.
   */
  public ImageMetaData(String albumID, long imageSize) {
    this.albumID = albumID;
    this.imageSize = imageSize;
  }

  /**
   * Gets the unique identifier of the album to which the image belongs.
   *
   * @return The album ID.
   */
  public String getAlbumID() {
    return albumID;
  }

  /**
   * Sets the unique identifier of the album to which the image belongs.
   *
   * @param albumID The album ID to set.
   */
  public void setAlbumID(String albumID) {
    this.albumID = albumID;
  }

  /**
   * Gets the size of the image in bytes.
   *
   * @return The size of the image.
   */
  public long getImageSize() {
    return imageSize;
  }

  /**
   * Sets the size of the image in bytes.
   *
   * @param imageSize The image size to set.
   */
  public void setImageSize(long imageSize) {
    this.imageSize = imageSize;
  }
}
