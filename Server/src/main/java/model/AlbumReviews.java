package model;

/**
 * Represents album reviews information, including the album ID and whether the user liked or disliked the album.
 * @author ambikakabra
 */
public class AlbumReviews {
  private String albumId;
  private String likeOrNot;

  /**
   * Constructs a new AlbumReviews instance.
   *
   * @param albumId   The unique identifier of the album.
   * @param likeOrNot A string indicating whether the user liked ("like") or disliked ("dislike") the album.
   */
  public AlbumReviews(String albumId, String likeOrNot) {
    this.albumId = albumId;
    this.likeOrNot = likeOrNot;
  }

  /**
   * Gets the album ID.
   *
   * @return The album ID.
   */
  public String getAlbumId() {
    return this.albumId;
  }

  /**
   * Sets the album ID.
   *
   * @param albumId The album ID to set.
   */
  public void setAlbumId(String albumId) {
    this.albumId = albumId;
  }

  /**
   * Gets whether the user liked or disliked the album.
   *
   * @return A string indicating "like" or "dislike".
   */
  public String getLikeOrNot() {
    return this.likeOrNot;
  }

  /**
   * Sets whether the user liked or disliked the album.
   *
   * @param likeOrNot A string indicating "like" or "dislike".
   */
  public void setLikeOrNot(String likeOrNot) {
    this.likeOrNot = likeOrNot;
  }
}