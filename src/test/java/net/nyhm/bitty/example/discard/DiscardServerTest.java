package net.nyhm.bitty.example.discard;

import org.junit.Test;

public class DiscardServerTest {
  @Test
  public void testDiscardServer() throws Exception {
    new DiscardServer(8888).run();
  }
}
