package net.nyhm.bitty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * {@link ServerLogic} responds to a {@link ClientRequest} via {@link ServerResponse#respond(String)}.
 */
public final class ServerResponse
{
    private final ChannelHandlerContext mContext;
    private final HttpRequest mRequest;

    ServerResponse(ChannelHandlerContext ctx, HttpRequest req)
    {
        mContext = ctx;
        mRequest = req;
    }

    public void respond(String responseBody)
    {
        respond(responseBody, true);
    }

    public void respond(String responseBody, boolean ok)
    {
        ByteBuf buf = Unpooled.copiedBuffer(responseBody, CharsetUtil.UTF_8);
        HttpResponseStatus status = ok ? OK : INTERNAL_SERVER_ERROR;
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, buf);

        // TODO: Do not assume Json!! Consider a ResponseFactory that produces responses for a particular type
        // (or specify type at HttpServer construction); If type is to be hard-coded as Json, then respond
        // should take Json, not String.
        //
        response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");

        if (isKeepAlive(mRequest))
        {
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            mContext.write(response);
        }
        else
        {
            mContext.write(response).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
