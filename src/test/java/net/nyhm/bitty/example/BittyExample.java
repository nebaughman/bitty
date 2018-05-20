package net.nyhm.bitty.example;

import net.nyhm.bitty.ClientRequest;
import net.nyhm.bitty.HttpServer;
import net.nyhm.bitty.ServerLogic;
import net.nyhm.bitty.ServerResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BittyExample
{
  private static final Logger log = LoggerFactory.getLogger(BittyExample.class);

  private static final int PORT = 8888;

  @Test
  public void runExample() throws Exception {
    ServerLogic serverLogic = new ExampleServerLogic("Hello");
    HttpServer server = new HttpServer(serverLogic, PORT, 1, 1);

    ExecutorService exec = Executors.newSingleThreadExecutor();
    Future<?> future = exec.submit(() -> {
      try {
        server.start();
      } catch (Exception e) {
        log.warn("Exception in server", e);
        server.stop();
      }
    });

    Thread.sleep(2000);

    URI uri = new URIBuilder()
        .setScheme("http")
        .setHost("localhost")
        .setPort(PORT)
        .build();

    try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
      HttpGet request = new HttpGet(uri);
      HttpResponse response = client.execute(request);
      HttpEntity entity = response.getEntity();
      String content = EntityUtils.toString(entity);
      System.out.println(content);
    }

    server.stop();
    future.cancel(true);
    exec.shutdownNow();
  }

  private static final class ExampleServerLogic implements ServerLogic
  {
    private final String responseMessage;

    ExampleServerLogic(String responseMessage) {
      this.responseMessage = responseMessage;
    }

    @Override
    public void processRequest(ClientRequest request, ServerResponse response) {
      log.info("Client request");
      request.logRequest();
      response.respond(responseMessage);
    }
  }
}
