package net.nyhm.bitty.example;

import io.netty.util.CharsetUtil;
import net.nyhm.bitty.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static org.junit.Assert.assertEquals;

public class BittyExample
{
  private static final Logger log = LoggerFactory.getLogger(BittyExample.class);

  private static final int PORT = 8888;

  private static final String MESSAGE = "Hello";

  @Test
  public void testResponse() throws Exception {
    ContentType contentType = new ContentType(
        new MimeType("text", "plain"),
        CharsetUtil.UTF_8
    );
    ServerLogic logic = new CannedResponseLogic(MESSAGE, contentType);
    HttpServer server = new HttpServer(logic, PORT, 1, 1);
    BittyService  service = new BittyService(server);
    service.start();
    assertEquals(MESSAGE, sendGet());
    service.stop();
  }

  @Test
  public void testEcho() throws Exception {
    BittyService service = new BittyService(
        new HttpServer(new EchoLogic(), PORT, 1, 1)
    );
    service.start();
    String response = sendPost(MESSAGE);
    log.info(response.getClass().getName());
    assertEquals(MESSAGE, response);
    service.stop();
  }

  @Test
  public void testIgnore() throws Exception {
    try (BittyService service = buildService(new IgnoreLogic())) {
      service.start();
      sendGet();
    }
  }

  /**
   * Test throwing an exception from ServerLogic. Server should continue to operate.
   */
  @Test
  public void testException() throws Exception {
    try (BittyService service = buildService(new FailOnce(new EchoLogic()))) {
      service.start();
      try {
        log.info("Second attempt");
        sendGet(); // this should fail (FailOnce logic)
      } catch (Exception e) {
        log.info("Expected exception", e);
      }
      log.info("Second attempt");
      String response = sendPost(MESSAGE); // this should work
      assertEquals(MESSAGE, response);
    }
  }

  /**
   * Send a GET request to the serverUri()
   */
  private static String sendGet() throws Exception {
    try (CloseableHttpClient client = buildClient()) {
      HttpGet request = new HttpGet(serverUri());
      HttpResponse response = client.execute(request);
      HttpEntity entity = response.getEntity();
      String content = EntityUtils.toString(entity);
      return content;
    }
  }

  /**
   * Send a POST request with the given body to the serverUri()
   */
  private static String sendPost(String body) throws Exception {
    try (CloseableHttpClient client = buildClient()) {
      HttpEntity entity = new StringEntity(body, CharsetUtil.UTF_8);
      HttpPost request = new HttpPost(serverUri());
      request.setEntity(entity);
      HttpResponse response = client.execute(request);
      String content = EntityUtils.toString(response.getEntity());
      return content;
    }
  }

  /**
   * Test service endpoint
   */
  private static URI serverUri() throws Exception {
    return new URIBuilder()
        .setScheme("http")
        .setHost("localhost")
        .setPort(PORT)
        .build();
  }

  private static CloseableHttpClient buildClient() {
    return HttpClientBuilder.create()
        .disableAutomaticRetries()
        .build();
  }

  private static BittyService buildService(ServerLogic logic) {
    return new BittyService(new HttpServer(logic, PORT, 1, 1));
  }

  /**
   * Always returns the specified content
   */
  private static final class CannedResponseLogic implements ServerLogic
  {
    private final String responseMessage;
    private final ContentType contentType;

    CannedResponseLogic(String responseMessage, ContentType contentType) {
      this.responseMessage = responseMessage;
      this.contentType = contentType;
    }

    @Override
    public void processRequest(ClientRequest request, ServerResponse response) throws Exception {
      log.info("Client request");
      request.logRequest();
      response.setContentType(contentType);
      response.respond(responseMessage);
    }
  }

  /**
   * Echoes the content of the request body (only for POST)
   */
  private static final class EchoLogic implements ServerLogic {
    @Override
    public void processRequest(ClientRequest request, ServerResponse response) throws Exception {
      String body = request.getBody();
      log.info("Client request", body);
      request.logRequest();
      response.setContentType(new ContentType(
          new MimeType("text", "plain"),
          CharsetUtil.UTF_8
      ));
      response.respond(body);
    }
  }

  /**
   * Sends back empty content (otherwise ignored)
   */
  private static final class IgnoreLogic implements ServerLogic {
    @Override
    public void processRequest(ClientRequest request, ServerResponse response) throws Exception {
      log.info("Ignoring request", request);
      response.respond(""); // must respond, even with nothing
    }
  }

  /**
   * Fail the first client request, then proxy the rest to another ServerLogic
   */
  private static final class FailOnce implements ServerLogic
  {
    private int mFailCount = 0;
    private ServerLogic mFallback;

    FailOnce(ServerLogic fallback) {
      mFallback = fallback;
    }

    @Override
    public void processRequest(ClientRequest request, ServerResponse response) throws Exception {
      if (mFailCount < 1) {
        mFailCount++;
        throw new Exception("Testing exception");
      } else {
        mFallback.processRequest(request, response);
      }
    }
  }
}
