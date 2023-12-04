package model;

/**
 * The AlbumInfo class represents information about an album, including the artist, title, and release year.
 * @author ambikakabra
 */
public class AlbumInfo {
  private String albumID;
  private String artist;
  private String title;
  private String year;

  /**
   * Constructs an AlbumInfo object with the specified artist, title, and release year.
   *
   * @param artist The name of the artist or band.
   * @param title The title of the album.
   * @param year The release year of the album.
   */
  public AlbumInfo(String artist, String title, String year) {
    this.artist = artist;
    this.title = title;
    this.year = year;
  }

  public AlbumInfo(String albumID, String artist, String title, String year) {
    this.albumID = albumID;
    this.artist = artist;
    this.title = title;
    this.year = year;
  }

  public AlbumInfo() {
  }

  public String getAlbumID() {
    return albumID;
  }

  public void setAlbumID(String albumID) {
    this.albumID = albumID;
  }

  /**
   * Gets the artist or band name of the album.
   *
   * @return The artist or band name.
   */
  public String getArtist() {
    return artist;
  }

  /**
   * Gets the title of the album.
   *
   * @return The album title.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Gets the release year of the album.
   *
   * @return The release year.
   */
  public String getYear() {
    return year;
  }

  /**
   * sets the artist
   * @param artist the name of artist or band name
   */
  public void setArtist(String artist) {
    this.artist = artist;
  }

  /**
   * sets the title
   * @param title the title of the album
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * sets the release year of the album.
   * @param year the release year of the album.
   */
  public void setYear(String year) {
    this.year = year;
  }
}

