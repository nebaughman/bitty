package net.nyhm.bitty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
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

    private ContentType mContentType = null;

    ServerResponse(ChannelHandlerContext ctx, HttpRequest req)
    {
        mContext = ctx;
        mRequest = req;
    }

    public void setContentType(ContentType contentType) {
        mContentType = contentType;
    }

    // TODO: Allow sending non-text responses

    public void respond(String responseBody)
    {
        respond(responseBody, true);
    }

    public void respond(String responseBody, boolean ok)
    {
        ByteBuf buf = Unpooled.copiedBuffer(responseBody, CharsetUtil.UTF_8);
        HttpResponseStatus status = ok ? OK : INTERNAL_SERVER_ERROR;
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, buf);

        if (mContentType != null) {
            response.headers().set(CONTENT_TYPE, mContentType.toHeaderValue());
        }

        if (HttpUtil.isKeepAlive(mRequest))
        {
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            mContext.write(response);
        }
        else
        {
            mContext.write(response).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
