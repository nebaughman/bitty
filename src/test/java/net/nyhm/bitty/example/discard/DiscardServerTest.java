package net.nyhm.bitty.example.discard;

import org.junit.Test;

/**
 * Warning: This is not a useful test, as it just sits and waits forever.
 * It was instructive to see the debug logging from the handler, and serves
 * as a simple example of a Netty HTTP service.
 *
 * @deprecated To be removed from this project
 */
public class DiscardServerTest {
  @Test
  public void testDiscardServer() throws Exception {
    new DiscardServer(8888).run();
  }
}
