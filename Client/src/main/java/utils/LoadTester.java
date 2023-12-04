package utils;

import com.google.gson.Gson;
import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LoadTester {
  private static final int MAX_RETRIES = 5;
  private static final int STATUS_CODE_400 = 400;

  public static void initialCalls(String IPAddr, int threadGroupSize, int numThreadGroups, int delay)
      throws InterruptedException, IOException {
    Long startTime;
    IPAddr = IPAddr.trim();

    HttpClient httpClient = HttpClient.newHttpClient();
    HttpRequest postAlbumRequest = postAlbumAPIRequest(IPAddr);


    List<Thread> threads = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      Thread thread = new Thread(() -> {
        for (int j = 0; j < 100; j++) {
          int tryCount = 0;
          boolean isSuccess = Boolean.FALSE;
          while (!isSuccess && tryCount < MAX_RETRIES) {
            try {
              HttpResponse<String> postResponse = httpClient.send(postAlbumRequest, HttpResponse.BodyHandlers.ofString());

              // Check the response codes for errors
              if (postResponse.statusCode() >= STATUS_CODE_400) {
                tryCount++;
              } else {
                isSuccess = Boolean.TRUE;
              }
            } catch (Exception e) {
              tryCount++;
              System.out.println("Post Request failed! Retrying again.");
            }
          }

          if (!isSuccess) {
            System.out.println("Post Request failed after 5 tries.");
          }
        }
      });
      threads.add(thread);
      thread.start();

      //joining the threads
      for (Thread t : threads) {
        try {
          t.join();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    startTime = System.currentTimeMillis();
    loadTesting(threadGroupSize, numThreadGroups, IPAddr, delay, startTime, postAlbumRequest);
  }

  private static void loadTesting(int threadGroupSize, int numThreadGroups, String ipAddr, int delay, Long startTime, HttpRequest postAlbumRequest)
      throws InterruptedException, IOException {
    HttpClient httpClient = HttpClient.newHttpClient();
    ConcurrentLinkedQueue<Response> responses = new ConcurrentLinkedQueue<>();
    List<Thread> threads = new ArrayList<>();
    for (int group = 0; group < numThreadGroups; group++) {
      for (int i = 0; i < threadGroupSize; i++) {
        Thread thread = new Thread(() -> {
          for (int j = 0; j < 100; j++) {
            int tryCount = 0;
            boolean isSuccess = Boolean.FALSE;
            while (!isSuccess && tryCount < MAX_RETRIES) {
              try {
                long postStartTime = System.currentTimeMillis();
                HttpResponse<String> postResponse = httpClient.send(postAlbumRequest, HttpResponse.BodyHandlers.ofString());
                ImageMetaData responseBody = new Gson().fromJson(postResponse.body(), ImageMetaData.class);
                String albumID = responseBody.getAlbumID();
                HttpRequest reviewLikeRequest = postReviewAPIRequest(ipAddr, "like", albumID);
                HttpRequest reviewDislikeRequest = postReviewAPIRequest(ipAddr, "dislike", albumID);
                long postEndTime = System.currentTimeMillis();
                long getStartTimeLike1 = System.currentTimeMillis();
                HttpResponse<String> reviewPostResponse1 = httpClient.send(reviewLikeRequest, HttpResponse.BodyHandlers.ofString());
                long getEndTimeLike1 = System.currentTimeMillis();
                long getStartTimeLike2 = System.currentTimeMillis();
                HttpResponse<String> reviewPostResponse2 = httpClient.send(reviewLikeRequest, HttpResponse.BodyHandlers.ofString());
                long getEndTimeLike2 = System.currentTimeMillis();
                long getStartTimeDislike1 = System.currentTimeMillis();
                HttpResponse<String> reviewPostResponse3 = httpClient.send(reviewDislikeRequest, HttpResponse.BodyHandlers.ofString());
                long getEndTimeDislike1 = System.currentTimeMillis();
                if (reviewPostResponse3.statusCode() >= STATUS_CODE_400
                    && reviewPostResponse1.statusCode() >= STATUS_CODE_400
                    && reviewPostResponse2.statusCode() >= STATUS_CODE_400
                    && postResponse.statusCode() >= STATUS_CODE_400) {
                  tryCount++;
                } else {
                  isSuccess = Boolean.TRUE;
                }

                responses.add(new Response(postStartTime, "POST", postEndTime - postStartTime, postResponse.statusCode()));
                responses.add(new Response(getStartTimeLike1, "POST", getEndTimeLike1 - getStartTimeLike1, reviewPostResponse1.statusCode()));
                responses.add(new Response(getStartTimeLike1, "POST", getEndTimeLike2 - getStartTimeLike2, reviewPostResponse2.statusCode()));
                responses.add(new Response(getStartTimeLike1, "POST", getEndTimeDislike1 - getStartTimeDislike1, reviewPostResponse2.statusCode()));
              } catch (Exception e) {
                tryCount++;
                System.out.println("Post Request failed! Retrying again.");
              }
            }

            if (!isSuccess) {
              System.out.println("Post Request failed after 5 tries.");
            }
          }
        });
        threads.add(thread);
        thread.start();
      }
      Thread.sleep(delay * 1000);
    }

    //joining the threads
    for (Thread t : threads) {
      try {
        t.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    long[] postLatencies = responses.stream()
        .filter(r -> r.getRequestType().equals("POST"))
        .mapToLong(Response::getLatency)
        .sorted().toArray();


    printStatistics(threadGroupSize, numThreadGroups, startTime, postLatencies, responses);
  }

  /**
   * Prints statistics for the executed requests.
   *
   * @param threadGroupSize  The size of each thread group.
   * @param numThreadGroups  The number of thread groups.
   * @param startTime        The start time of the execution.
   * @throws IOException If an I/O error occurs.
   */
  private static void printStatistics(int threadGroupSize, int numThreadGroups, Long startTime, long[] postLatencies, ConcurrentLinkedQueue<Response> responses)
      throws IOException {
    long endTime = System.currentTimeMillis();
    int totalRequests = responses.size() + 1000;
    long wallTime = (endTime - startTime) / 1000;
    double totalLatency = responses.stream().mapToLong(Response::getLatency).sum();
    double meanLatency = calculateMeanLatency(responses);
    double medianLatency = calculateMedianLatency(responses, totalRequests);
    double p99Latency = calculateP99Latency(responses, totalRequests);
    double minResponseTime = responses.stream().mapToLong(Response::getLatency).min().orElse(0);
    double maxResponseTime = responses.stream().mapToLong(Response::getLatency).max().orElse(0);
    double minResponseTimePOST = calculateMinResponseTimeForType(responses, "POST");
    double maxResponseTimePOST = calculateMaxResponseTimeForType(responses, "POST");
    double successfulRequests = responses.stream().filter(r -> r.getStatusCode() == 200 || r.getStatusCode() == 201).count() + 1000;
    double meanPostLatency = calculateMeanPostLatency(postLatencies);
    double medianPostLatency = calculateMedianPostLatency(postLatencies);
    double p99PostLatency = calculateP99PostLatency(postLatencies);

    // Print statistics
    System.out.println("Total requests: " + totalRequests);
    System.out.println("Successful requests: " + successfulRequests);
    System.out.println("Unsuccessful requests: " + (totalRequests - successfulRequests));
    System.out.println("\n\nOverall statistics:\n");
    System.out.println("Wall time: " + wallTime + " seconds");
    System.out.println("avg. Throughput: " + (double) totalRequests / wallTime + " requests/second");
    System.out.println("Mean total latency Total: " + meanLatency + " ms");
    System.out.println("Median total latency Total: " + medianLatency + " ms");
    System.out.println("99th percentile latency: " + p99Latency + " ms");
    System.out.println("Min response time: " + minResponseTime + " ms");
    System.out.println("Max response time: " + maxResponseTime + " ms\n\n");
    System.out.println("POST request statistics:\n");
    System.out.println("Mean POST latency: " + meanPostLatency + " ms");
    System.out.println("Median POST latency: " + medianPostLatency + " ms");
    System.out.println("99th percentile POST latency: " + p99PostLatency + " ms");
    System.out.println("Min response time POST: " + minResponseTimePOST + " ms");
    System.out.println("Max response time POST: " + maxResponseTimePOST + " ms\n\n");

    // Writing to CSV file
    writeResponsesToCSV(responses);
  }

  private static void writeResponsesToCSV(ConcurrentLinkedQueue<Response> responses) throws IOException {
    try (FileWriter outputFile = new FileWriter("./src/main/resources/LoadResults.csv");
        CSVWriter writer = new CSVWriter(outputFile)) {

      writer.writeNext(new String[]{"Start Time", "Request Type", "Latency", "Status Code"});

      for (Response response : responses) {
        writer.writeNext(new String[]{String.valueOf(response.getStartTime()), response.getRequestType().toString(),
            String.valueOf(response.getLatency()), String.valueOf(response.getStatusCode())});
      }
    }
  }

  private static double calculateMeanLatency(ConcurrentLinkedQueue<Response> responses) {
    return responses.stream().mapToLong(Response::getLatency).average().orElse(0.0);
  }

  private static double calculateMedianLatency(ConcurrentLinkedQueue<Response> responses, int totalRequests) {
    return responses.stream().sorted(Comparator.comparingLong(Response::getLatency))
        .skip(totalRequests / 2).findFirst().orElse(new Response(0, "", 0, 0)).getLatency();
  }

  private static double calculateP99Latency(ConcurrentLinkedQueue<Response> responses, int totalRequests) {
    return responses.stream().sorted(Comparator.comparingLong(Response::getLatency))
        .skip((int) (totalRequests * 0.99)).findFirst().orElse(new Response(0, "", 0, 0)).getLatency();
  }

  private static double calculateMinResponseTimeForType(ConcurrentLinkedQueue<Response> responses, String type) {
    return responses.stream().filter(r -> r.getRequestType().equals(type)).mapToLong(Response::getLatency).min().orElse(0);
  }

  private static double calculateMaxResponseTimeForType(ConcurrentLinkedQueue<Response> responses, String type) {
    return responses.stream().filter(r -> r.getRequestType().equals(type)).mapToLong(Response::getLatency).max().orElse(0);
  }

  private static double calculateMeanPostLatency(long[] postLatencies) {
    return postLatencies.length > 0 ? (double) Arrays.stream(postLatencies).sum() / postLatencies.length : 0;
  }

  private static double calculateMedianPostLatency(long[] postLatencies) {
    return postLatencies.length > 0 ? postLatencies[(int) (postLatencies.length / 2)] : 0;
  }

  private static double calculateP99PostLatency(long[] postLatencies) {
    return postLatencies.length > 0 ? postLatencies[(int) (postLatencies.length * 0.99)] : 0;
  }

  public static HttpRequest postAlbumAPIRequest(String IPAddr) {
    // Specify the path to the image file you want to upload

    // Create a unique boundary string
    String boundary = UUID.randomUUID().toString();

    // Create a multipart request body
    HttpRequest request = null;
    try {
      request = HttpRequest.newBuilder()
          .uri(new URI(IPAddr + "/Server_war/albums"))
          .header("Content-Type", "multipart/form-data; boundary=" + boundary)
          .POST(BodyPublishers.ofByteArray(createMultipartRequest(boundary)))
          .build();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    return request;
  }

  // Create the multipart request body manually
  private static byte[] createMultipartRequest(String boundary) {
    String CRLF = "\r\n";
    String boundaryLine = "--" + boundary;

    String requestBody = boundaryLine + CRLF +
        "Content-Disposition: form-data; name=\"artist\"" + CRLF +
        CRLF +
        "ambika" + CRLF +
        boundaryLine + CRLF +
        "Content-Disposition: form-data; name=\"title\"" + CRLF +
        CRLF +
        "first album" + CRLF +
        boundaryLine + CRLF +
        "Content-Disposition: form-data; name=\"year\"" + CRLF +
        CRLF +
        "1993" + CRLF +
        boundaryLine + CRLF +
        "Content-Disposition: form-data; name=\"image\"; filename=\"nmtb.png\"" + CRLF +
        "Content-Type: image/png" + CRLF +
        CRLF;

    String endBoundary = CRLF + boundaryLine + "--" + CRLF;
    String fullRequestBody = requestBody + "FileContentHere" + endBoundary;

    return fullRequestBody.getBytes(StandardCharsets.UTF_8);
  }

  public static HttpRequest postReviewAPIRequest(String IPAddr, String likeornot, String albumID) {
    HttpRequest postReviewRequest = HttpRequest.newBuilder()
        .uri(URI.create(IPAddr + "/Server_war/review/"+likeornot+"/"+albumID))
        .POST(BodyPublishers.noBody())
        .build();

    return postReviewRequest;
  }
}
