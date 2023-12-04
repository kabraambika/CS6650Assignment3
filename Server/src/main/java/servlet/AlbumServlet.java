package servlet;

import com.google.gson.Gson;
import factory.ConnectionManager;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.AlbumInfo;
import model.ImageMetaData;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONObject;

/**
 * The AlbumServlet class is a servlet for handling HTTP requests related to albums, including retrieving album information and uploading new albums with images.
 *
 * @author ambikakabra
 */
@WebServlet(name = "AlbumServlet", value = "/albums/*")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 10,
    maxFileSize = 1024 * 1024 * 50,
    maxRequestSize = 1024 * 1024 * 100
)
public class AlbumServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final Gson gson = new Gson();
  /**
   * Handles GET requests for album information.
   *
   * @param request  The HTTP request.
   * @param response The HTTP response.
   * @throws IOException      If an I/O error occurs.
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");

    String urlPath = request.getPathInfo();
    String servletPath = request.getServletPath();

    if (servletPath == null || servletPath.isEmpty()) {
      sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Missing parameters");
      return;
    }

    if (!isUrlValid(urlPath)) {
      sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request");
      return;
    }
    String albumId = urlPath.split("/")[1];

    //get albumInfo from Database
    AlbumInfo albumInfo = new ConnectionManager().getAlbumById(albumId);
    if (albumInfo != null) {
      sendAlbumInfoResponse(albumInfo, response);
    } else {
      sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Key not found");
    }
  }

  /**
   * Handles POST requests for uploading image and album information and returns ImageMetaData.
   *
   * @param request  The HTTP request.
   * @param response The HTTP response.
   * @throws IOException      If an I/O error occurs.
   */
  @Override
  protected void doPost(
      HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    String servletPath = request.getServletPath();

    if (!isUrlValid(servletPath)) {
      sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request");
      return;
    }

    FileItemFactory factory = new DiskFileItemFactory();
    ServletFileUpload upload = new ServletFileUpload(factory);
    try {
      List<FileItem> items = upload.parseRequest(request);
      byte[] imageBytes = new byte[0];
      AlbumInfo albumInfo = new AlbumInfo();

      for (FileItem item : items) {
        if (item.isFormField()) {
          if ("artist".equals(item.getFieldName())) {
            albumInfo.setArtist(item.getString());
          }
          else if ("title".equals(item.getFieldName())) {
            albumInfo.setTitle(item.getString());
          }
          else if ("year".equals(item.getFieldName())) {
            albumInfo.setYear(item.getString());
          }
        } else {
          if ("image".equals(item.getFieldName())) {
            imageBytes = item.get();
          }
        }
      }

      if(albumInfo != null && validateAlbumInfo(albumInfo, imageBytes)) {
        ImageMetaData imageMetaData = new ConnectionManager().saveAlbum(albumInfo, imageBytes);
        if(imageMetaData != null) {
          sendImageInfoResponse(response, new JSONObject(imageMetaData));
        }
        else {
          sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while saving image and album info");
        }
      }
      else {
        sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid or missing parameter! Must add artist, title, year, image");
      }
    } catch (FileUploadException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Validates the completeness of album information and image data.
   *
   * @param album The AlbumInfo object to be validated.
   * @param imageBytes The binary image data to be validated.
   * @return true if the album information and image data are complete and valid; false otherwise.
   */
  private boolean validateAlbumInfo(AlbumInfo album, byte[] imageBytes) {
    return album.getArtist() != null && album.getTitle() != null && album.getYear() != null && imageBytes.length != 0;
  }

  /**
   * Validates URL path
   * @param urlPath Path of url
   * @return does url matches or not, boolean
   */
  private boolean isUrlValid(String urlPath) {
    for (Endpoint endpoint : Endpoint.values()) {
      Pattern pattern = endpoint.pattern;

      if (pattern.matcher(urlPath).matches()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Helper method to write and send message and status in response
   * @param response The HTTP response.
   * @param status The HTTP response status code.
   * @param message The HTTP response message.
   * @throws IOException If an I/O error occurs.
   */
  private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
    response.setStatus(status);
    response.getWriter().write(message);
  }

  /**
   * Helper method to send ImageMetaData in Response
   * @param response The HTTP response.
   * @throws IOException If an I/O error occurs.
   */
  private void sendAlbumInfoResponse(AlbumInfo albumInfo, HttpServletResponse response) throws IOException {

    response.setStatus(HttpServletResponse.SC_OK);
    JSONObject jsonObject = new JSONObject(albumInfo);
    response.getWriter().write(jsonObject.toString());
  }

  /**
   * Helper method to send image info in response
   * @param response The HTTP response.
   * @param jsonObject JSONObject of album id and image size
   * @throws IOException If I/O error occurs
   */
  private void sendImageInfoResponse(HttpServletResponse response, JSONObject jsonObject) throws IOException {
    response.setStatus(HttpServletResponse.SC_OK);
    response.getWriter().write(jsonObject.toString());
  }

  /**
   * Enumeration representing different URL endpoints.
   */
  private enum Endpoint {
    POST_NEW_ALBUM(Pattern.compile("/albums")),
    GET_ALBUM_BY_KEY(Pattern.compile("^/[a-zA-Z0-9]+$"));
    public final Pattern pattern;
    Endpoint(Pattern pattern) {
      this.pattern = pattern;
    }
  }

}
