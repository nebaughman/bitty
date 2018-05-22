package net.nyhm.bitty.example;

import io.netty.util.CharsetUtil;
import net.nyhm.bitty.*;
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

import static org.junit.Assert.assertEquals;

public class BittyExample
{
  private static final Logger log = LoggerFactory.getLogger(BittyExample.class);

  private static final int PORT = 8888;

  private static final String MESSAGE = "Hello";

  @Test
  public void runExample() throws Exception {
    ServerLogic serverLogic = new ExampleServerLogic(MESSAGE);
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
      assertEquals(content, MESSAGE);
    }

    server.stop();
    future.cancel(true);
    exec.shutdown();
  }

  private static final class ExampleServerLogic implements ServerLogic
  {
    private final String responseMessage;

    ExampleServerLogic(String responseMessage) {
      this.responseMessage = responseMessage;
    }

    @Override
    public void processRequest(ClientRequest request, ServerResponse response) throws Exception {
      log.info("Client request");
      request.logRequest();
      response.setContentType(new ContentType(
          new MimeType("text", "plain"),
          CharsetUtil.UTF_8
      ));
      response.respond(responseMessage);
    }
  }
}
