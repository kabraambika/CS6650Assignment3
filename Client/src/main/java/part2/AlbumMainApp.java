package part2;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import utils.CmdLineInfo;
import utils.CmdLineParser;
import utils.ImageMetaData;
import utils.InvalidArgumentsException;
import utils.LoadTester;

/**
 * The part2.AlbumMainApp class is the entry point of the application for sending album requests.
 *
 * @author ambikakabra
 */
public class AlbumMainApp {
  /**
   * The main method of the application.
   *
   * @param args The command-line arguments provided to the application.
   * @throws IOException              If an I/O error occurs.
   * @throws InterruptedException     If the execution is interrupted.
   * @throws InvalidArgumentsException If invalid command-line arguments are provided.
   */
  public static void main(String[] args)
      throws IOException, InterruptedException, InvalidArgumentsException, URISyntaxException {
    // Parse command-line arguments

    CmdLineParser cmdLineParser = new CmdLineParser();
    CmdLineInfo cmdLineInfo = cmdLineParser.parseInfo(args);

    // Get command-line parameters
    Map<String, Integer> params = cmdLineInfo.getCommandLineInfo();
    int threadGroupSize = params.get("threadGroupSize");
    int numThreadGroups = params.get("numThreadGroups");
    int delay = params.get("delay");
    String IPAddr = cmdLineInfo.getServerPath();
    LoadTester.initialCalls(IPAddr,threadGroupSize, numThreadGroups, delay);
  }
}