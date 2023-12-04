package utils;

import utils.CmdLineInfo;
import utils.InvalidArgumentsException;

/**
 * utils.CmdLineParser class is used to parse the args provided while running the app.
 *
 * @author ambikakabra
 */
public class CmdLineParser {
  private final CmdLineInfo cmdLineInfo;

  /**
   * Constructor of utils.CmdLineParser
   */
  public CmdLineParser() {
    cmdLineInfo = new CmdLineInfo();
  }

  /**
   * This method is used to parse information provided in arrays of arguments
   * @param args Array of strings provided while running app
   * @return Instance of utils.CmdLineInfo
   * @throws InvalidArgumentsException if length of args is less than 8 then throws this exception
   */
  public CmdLineInfo parseInfo(String[] args) throws InvalidArgumentsException {
    int index = 0;

    if(args.length < 8) {
      throw new InvalidArgumentsException("Arguments missing! accepts 4 arguments -threadGroupSize 10 -numThreadGroups 100 -delay 10 -IPAddr <server URI>.");
    }
    while (index < args.length) {
      String currentArg = args[index];
      if (isFlag(currentArg) && cmdLineInfo.validateCmd(currentArg.substring(1))) {
        handleFlag(currentArg, args[index + 1]);
        index += 2;
      } else {
        index++;
      }
    }
    return cmdLineInfo;
  }

  /**
   * This helper method is used to identify flag before arg name
   * @param arg name of argument
   * @return boolean, whether arg is flagged or not
   */
  private boolean isFlag(String arg) {
    return arg.startsWith("-");
  }

  /**
   * This helper method is used to handle flagged args and store them in cmdLineInfo instance's map
   * @param flag arg in String
   * @param value value of arg provided in args
   */
  private void handleFlag(String flag, String value) {
    if ("-IPAddr".equals(flag)) {
      cmdLineInfo.setServerPath(value);
    } else {
      cmdLineInfo.setCmdLineInfo(flag.substring(1), value);
    }
  }
}
