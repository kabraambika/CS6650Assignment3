package utils;

import java.util.HashMap;
import java.util.Map;

/**
 * utils.CmdLineInfo class stores command-line information and provides methods to access and modify it.
 *
 * @author ambikakabra
 */
public class CmdLineInfo {
  // Constants for default values and keys
  private static final int DEFAULT_THREAD_GROUP_SIZE = 10;
  private static final int DEFAULT_NUM_THREAD_GROUPS = 100;
  private static final int DEFAULT_DELAY = 0;
  private static final String DEFAULT_IP_ADDR = "localhost";
  private static final int DEFAULT_SERVER_PORT = 8080;
  private static final String THREAD_GROUP_SIZE_KEY = "threadGroupSize";
  private static final String NUM_THREAD_GROUPS_KEY = "numThreadGroups";
  private static final String DELAY_KEY = "delay";
  private static final String IP_ADDR_KEY = "IPAddr";

  private Map<String, Integer> commandLineInfo;
  private String serverPath;
  private String ipAddr;

  /**
   * Initializes utils.CmdLineInfo with default values.
   */
  public CmdLineInfo() {
    this.commandLineInfo = new HashMap<>();
    this.commandLineInfo.put(THREAD_GROUP_SIZE_KEY, DEFAULT_THREAD_GROUP_SIZE);
    this.commandLineInfo.put(NUM_THREAD_GROUPS_KEY, DEFAULT_NUM_THREAD_GROUPS);
    this.commandLineInfo.put(DELAY_KEY, DEFAULT_DELAY);
    this.ipAddr = DEFAULT_IP_ADDR;
    this.serverPath = "http://" + this.ipAddr + ":" + DEFAULT_SERVER_PORT;
  }

  /**
   * Gets the command-line information map.
   *
   * @return The command-line information map.
   */
  public Map<String, Integer> getCommandLineInfo() {
    return commandLineInfo;
  }

  /**
   * Gets the server path.
   *
   * @return The server path.
   */
  public String getServerPath() {
    return serverPath;
  }

  /**
   * Gets the IP address.
   *
   * @return The IP address.
   */
  public String getIpAddr() {
    return ipAddr;
  }

  /**
   * Sets a specific command-line information.
   *
   * @param cmdName The name of the command to set.
   * @param value   The value to set for the command.
   */
  public void setCmdLineInfo(String cmdName, String value) {
    Integer val = Integer.valueOf(value);
    this.commandLineInfo.put(cmdName, val);
  }

  /**
   * Sets the server path based on the given IP address.
   *
   * @param ipAddress The IP address to set.
   */
  public void setServerPath(String ipAddress) {
    this.serverPath = "http://" + ipAddress + ":" + DEFAULT_SERVER_PORT;
  }

  /**
   * Validates if a command is a valid command name.
   *
   * @param cmdName The command name to validate.
   * @return True if the command is valid; otherwise, false.
   */
  public boolean validateCmd(String cmdName) {
    return this.commandLineInfo.keySet().contains(cmdName) || cmdName.equals(IP_ADDR_KEY);
  }
}