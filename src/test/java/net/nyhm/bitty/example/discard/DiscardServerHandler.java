package net.nyhm.bitty.example.discard;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Taken directly from https://netty.io/wiki/user-guide-for-4.x.html
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter {

  private static Logger log = LoggerFactory.getLogger(DiscardServerHandler.class);

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    log.info("Received " + msg);
    ReferenceCountUtil.release(msg);
  }

  /**
   * Closes the connection when an exception is raised.
   */
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    log.warn("Handler exception", cause);
    cause.printStackTrace();
    ctx.close();
  }
}
