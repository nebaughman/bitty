package net.nyhm.bitty;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * This class is used as the top of the Netty protocol handling stack, acting as the
 * bridge to the user-provided ServerLogic instance.
 */
final class HttpServerHandler extends ChannelInboundHandlerAdapter
{
    private static final Logger log = LoggerFactory.getLogger(HttpServerHandler.class);

    private final ServerLogic mLogic;

    HttpServerHandler(ServerLogic logic)
    {
        mLogic = logic;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx)
        throws Exception
    {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        try {
            if (msg instanceof HttpRequest) {
                processRequest(ctx, (HttpRequest) msg);
            } else {
                log.warn("Unexpected message type received", msg.getClass());
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void processRequest(ChannelHandlerContext ctx, HttpRequest req) throws Exception
    {
        if (req.decoderResult().isFailure()) // TODO: what other failure cases to handle here?
        {
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        }
        else if (!(req instanceof FullHttpRequest))
        {
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR);
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        }
        else
        {
            ClientRequest request = new ClientRequest(ctx, (FullHttpRequest)req);
            ServerResponse response = new ServerResponse(ctx, req);
            mLogic.processRequest(request, response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
        throws Exception
    {
        log.warn("Exception handling request", cause);
        ctx.close();
    }
}
