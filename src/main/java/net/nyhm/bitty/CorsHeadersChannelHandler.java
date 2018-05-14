package net.nyhm.bitty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpResponse;

/**
 * CORS headers allow cross-origin HTTP requests (for scripts).
 */
public class CorsHeadersChannelHandler extends ChannelOutboundHandlerAdapter
{
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception
    {
        if (msg instanceof HttpResponse)
        {
            HttpResponse response = (HttpResponse)msg;
            response.headers().set("Access-Control-Allow-Origin", "*");
            response.headers().set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            response.headers().set("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, Content-Length");
        }
        ctx.write(msg, promise);
    }
}
