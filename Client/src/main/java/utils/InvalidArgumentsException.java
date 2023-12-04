package utils;

/**
 * Custom exception class for representing invalid command-line arguments.
 *
 * @author ambikakabra
 */
public class InvalidArgumentsException extends Exception {

  /**
   * Constructs a new utils.InvalidArgumentsException with the specified error message.
   *
   * @param message The error message that describes the invalid arguments.
   */
  public InvalidArgumentsException(String message) {
    super(message);
  }
}