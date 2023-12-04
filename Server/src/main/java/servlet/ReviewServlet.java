package servlet;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import factory.ConnectionManager;
import factory.RabbitMQConnectionManager;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import model.AlbumInfo;
import model.AlbumReviews;

/**
 * Servlet for handling album reviews using RabbitMQ for communication.
 *
 * @author ambikakabra
 */
@WebServlet(name = "ReviewServlet", value = "/review/*")
public class ReviewServlet extends HttpServlet {

    /** Number of threads for handling RabbitMQ message delivery. */
    private static final int NUM_THREADS = 20;

    /** Basic Quality of Service (QoS) for RabbitMQ channels. */
    private static final int BASIC_QOS = 50000;

    /** Default RabbitMQ queue name for album reviews. */
    private static final String QUEUE_NAME = "HW3";
    private static final ConnectionManager connectionManager = new ConnectionManager();
    private static final Channel channel = RabbitMQConnectionManager.getChannel();

    /**
     * Initializes the servlet by setting up RabbitMQ channels and message delivery threads.
     *
     * @throws ServletException If an error occurs during servlet initialization.
     */
    @Override
    public void init() throws ServletException {
        super.init();
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);

        try {
            final Channel channel = RabbitMQConnectionManager.getNewConnection().createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            final DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                try {
                    handleDelivery(delivery);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            };
            channel.basicQos(BASIC_QOS);

            for (int i = 0; i < NUM_THREADS; i++) {
                executorService.submit(() -> {
                    try {
                        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

        // Shutdown the executor when it's no longer needed
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executorService.shutdown();
            try {
                executorService.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
    }

    /**
     * Handles the delivery of RabbitMQ messages and updates album reviews accordingly.
     *
     * @param delivery The RabbitMQ message delivery.
     * @throws UnsupportedEncodingException If the message encoding is not supported.
     */
    private void handleDelivery(Delivery delivery) throws UnsupportedEncodingException {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        String[] messageParts = message.split(",");
        new ConnectionManager().updateReviews(messageParts);
    }

    /**
     * Handles HTTP POST requests for updating album reviews.
     *
     * @param request  The HTTP request.
     * @param response The HTTP response.
     * @throws IOException If an error occurs during I/O operations.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        String url = request.getPathInfo();
        String[] urlParts = url.split("/");

        if (isInvalidInput(urlParts)) {
            response.setStatus(400);
            response.getWriter().write("Invalid inputs");
            return;
        }

        String id = urlParts[2];

        AlbumInfo albumInfo = new ConnectionManager().getAlbumById(id);
        if (albumInfo == null) {
            response.setStatus(404);
            response.getWriter().write("Album not found");
            return;
        }

        AlbumReviews albumReviews = new AlbumReviews(id, urlParts[1]);
        Channel channel = RabbitMQConnectionManager.getChannel();

        String json = createJson(albumReviews);
        channel.basicPublish("", QUEUE_NAME, null, json.getBytes());

        response.setStatus(201);
        response.getWriter().write("Write successful");
    }

    /**
     * Creates a JSON representation of the album reviews based on user actions.
     *
     * @param albumReviews The album reviews information.
     * @return The JSON representation of the album reviews.
     */
    private String createJson(AlbumReviews albumReviews) {
        int value = albumReviews.getLikeOrNot().equals("like") ? 1 : -1;
        return albumReviews.getAlbumId() + "," + value;
    }

    /**
     * Checks if the input URL parts are valid for album reviews processing.
     *
     * @param urlParts The URL parts extracted from the request path.
     * @return True if the input is invalid, false otherwise.
     */
    private boolean isInvalidInput(String[] urlParts) {
        if (urlParts.length != 3) {
            return true;
        }
        String action = urlParts[1];
        String albumId = urlParts[2];

        return albumId.isEmpty() || (!action.equals("like") && !action.equals("dislike"));
    }
}
