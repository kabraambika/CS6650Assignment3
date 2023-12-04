package factory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Manages RabbitMQ connections and channels for communication.
 * @author ambikakabra
 */
public class RabbitMQConnectionManager {

  /** The default queue name for RabbitMQ communication. */
  private static final String QUEUE_NAME = "HW3";

  /** The RabbitMQ connection factory. */
  private static final ConnectionFactory factory = createFactory();

  /** The current RabbitMQ connection. */
  private static Connection connection;

  /** The current RabbitMQ channel. */
  private static Channel channel;

  /** The default RabbitMQ server host name. */
  private static final String HOST_NAME = "ec2-54-69-76-218.us-west-2.compute.amazonaws.com";

  /** The default RabbitMQ server port number. */
  private static final int HOST_PORT = 5672;

  // Private constructor to prevent instantiation
  private RabbitMQConnectionManager() { }

  /**
   * Creates and configures a new RabbitMQ connection factory.
   *
   * @return The configured RabbitMQ connection factory.
   */
  private static ConnectionFactory createFactory() {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(HOST_NAME);
    factory.setPort(HOST_PORT);
    return factory;
  }

  /**
   * Creates a new RabbitMQ connection.
   *
   * @return The new RabbitMQ connection.
   * @throws TimeoutException If a timeout occurs during connection creation.
   */
  private static Connection createConnection() throws TimeoutException {
    try {
      return factory.newConnection();
    } catch (IOException e) {
      throw new RuntimeException("Error creating a new connection", e);
    }
  }

  /**
   * Gets the configured RabbitMQ connection factory.
   *
   * @return The RabbitMQ connection factory.
   */
  public static ConnectionFactory getFactory() {
    return factory;
  }

  /**
   * Gets a new RabbitMQ connection.
   *
   * @return The new RabbitMQ connection.
   * @throws TimeoutException If a timeout occurs during connection creation.
   */
  public static Connection getNewConnection() throws TimeoutException {
    return createConnection();
  }

  /**
   * Gets the current RabbitMQ connection. Creates a new one if none exists or if the existing one is closed.
   *
   * @return The RabbitMQ connection.
   * @throws TimeoutException If a timeout occurs during connection creation.
   */
  public static Connection getConnection() throws TimeoutException {
    if (connection == null || !connection.isOpen()) {
      connection = createConnection();
    }
    return connection;
  }

  /**
   * Gets the default RabbitMQ queue name.
   *
   * @return The RabbitMQ queue name.
   */
  public static String getQueueName() {
    return QUEUE_NAME;
  }

  /**
   * Gets the current RabbitMQ channel. Creates a new one if none exists or if the existing one is closed.
   *
   * @return The RabbitMQ channel.
   * @throws RuntimeException If an error occurs during channel creation.
   */
  public static Channel getChannel() {
    if (channel == null || !channel.isOpen()) {
      try {
        channel = getConnection().createChannel();
      } catch (IOException | TimeoutException e) {
        throw new RuntimeException("Error creating a new channel", e);
      }
    }
    return channel;
  }
}