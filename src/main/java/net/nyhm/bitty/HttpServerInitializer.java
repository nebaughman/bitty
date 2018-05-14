package net.nyhm.bitty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;

import io.netty.channel.socket.SocketChannel;

import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

final class HttpServerInitializer extends ChannelInitializer<SocketChannel>
{
    // See http://netty.io/4.0/api/io/netty/channel/ChannelPipeline.html
    //static final io.netty.util.concurrent.EventExecutorGroup group =
    //    new io.netty.util.concurrent.DefaultEventExecutorGroup(16);

    private static final int MAX_HTTP_OBJECT_AGGREGATOR_LENGTH = 1048576;

    private final ServerLogic mLogic;
    private final SSLContext mSSL;

    HttpServerInitializer(ServerLogic logic, SSLContext ssl)
    {
        mLogic = logic;
        mSSL = ssl;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception
    {
        ChannelPipeline p = ch.pipeline();

        if (mSSL != null)
        {
            SSLEngine engine = mSSL.createSSLEngine();
            engine.setUseClientMode(false);
            p.addLast("ssl", new SslHandler(engine));
        }

        p.addLast("decoder", new HttpRequestDecoder());
        p.addLast("aggregator", new HttpObjectAggregator(MAX_HTTP_OBJECT_AGGREGATOR_LENGTH));
        p.addLast("encoder", new HttpResponseEncoder());
        p.addLast("deflater", new HttpContentCompressor());
        p.addLast("cors", new CorsHeadersChannelHandler());
        p.addLast("handler", new HttpServerHandler(mLogic));
    }
}


