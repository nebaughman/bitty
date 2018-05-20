package net.nyhm.bitty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;

public final class HttpServer
{
    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);

    private final ServerLogic mLogic;
    private final int mPort;
    private final int mBossThreads;
    private final int mWorkerThreads;
    private final SSLContext mSSL;

    private Channel mChannel = null;

    public HttpServer(ServerLogic logic, int port, int bossThreads, int workerThreads)
    {
        this(logic, port, bossThreads, workerThreads, null);
    }

    public HttpServer(ServerLogic logic, int port, int bossThreads, int workerThreads, SSLContext ssl)
    {
        mLogic = logic;
        mPort = port;
        mBossThreads = bossThreads;
        mWorkerThreads = workerThreads;
        mSSL = ssl;
    }

    public void start() throws Exception
    {
        EventLoopGroup bossGroup = new NioEventLoopGroup(mBossThreads,
            new NamedThreadFactory("http-boss"));

        EventLoopGroup workerGroup = new NioEventLoopGroup(mWorkerThreads,
            new NamedThreadFactory("http-worker"));

        try
        {
            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new HttpServerInitializer(mLogic, mSSL))
                .childOption(ChannelOption.SO_KEEPALIVE, true);
                //.option(ChannelOption.TCP_NODELAY, true);

            mChannel = b.bind(mPort).sync().channel();
            log.info("Listening on " + mChannel.localAddress());
            mChannel.closeFuture().sync();
        }
        finally
        {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            log.info("Http server stopped");
        }
    }

    public void stop()
    {
        if (mChannel != null)
        {
            mChannel.close();
        }
    }
}
