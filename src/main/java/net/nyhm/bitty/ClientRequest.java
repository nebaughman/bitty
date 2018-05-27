package net.nyhm.bitty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ClientRequest
{
    private final ChannelHandlerContext mContext;
    private final FullHttpRequest mRequest;

    private final Path mPath;
    private final Map<String,String> mArgs;

    ClientRequest(ChannelHandlerContext ctx, FullHttpRequest req)
    {
        mContext = ctx;
        mRequest = req;

        QueryStringDecoder query = new QueryStringDecoder(req.uri());
        mPath = Paths.get(query.path());
        mArgs = Collections.unmodifiableMap(getArgs(query));
    }

    public Path getPath()
    {
        return mPath;
    }

    public Map<String,String> getArgs()
    {
        return mArgs;
    }

    /**
     * Interpret the body of the request as a String. This is only for POST and PUT requests.
     */
    public String getBody() {
        HttpMethod method = mRequest.method();
        if (method == HttpMethod.POST || method == HttpMethod.PUT) {
            return mRequest.content().toString(CharsetUtil.UTF_8); // TODO: Charset specified in headers?
        } else {
            return null; // no content in this method
        }
    }

    // TODO: getContent() to expose raw bytes, also content type so handler can parse data

    public void logRequest()
    {
        logRequest(mContext, mRequest);
    }

    /**
     * Flatten query args into simplified name-value map. Any duplicate args are ignored.
     */
    private static Map<String,String> getArgs(QueryStringDecoder query)
    {
        Map<String,List<String>> params = query.parameters();
        Map<String,String> map = new LinkedHashMap<>();
        for (String key : params.keySet())
        {
            List<String> vals = params.get(key);
            if (!map.containsKey(key)) // else ignore duplicate
            {
                // may put key with no value, ignore if more than one value
                map.put(key, vals.isEmpty() ? "" : vals.get(0));
            }
        }
        return map;
    }

    private static void logRequest(ChannelHandlerContext ctx, HttpRequest req)
    {
        StringBuilder log = new StringBuilder();

        log.append("IP[");
        SocketAddress remote = ctx.channel().remoteAddress();
        if (remote instanceof InetSocketAddress)
        {
            InetSocketAddress inet = (InetSocketAddress)remote;
            log.append(inet.getAddress().getHostAddress());
        }
        else
        {
            log.append("unknown");
        }
        log.append("]");

        HttpHeaders headers = req.headers();

        //String realIP = headers.get("X-Real-IP");
        String realIP = headers.get("X-Forwarded-For");
        if (realIP != null)
        {
            log.append(" SourceIP[");
            log.append(realIP);
            log.append("]");
        }

        String agent = headers.get("User-Agent");
        log.append(" Agent[");
        log.append(agent == null ? "unknown" : agent);
        log.append("] ");

        log.append("Keep-Alive:");
        log.append(HttpUtil.isKeepAlive(req));
        log.append(" ");

        log.append(req.uri());

        LoggerFactory.getLogger(ClientRequest.class).debug(log.toString());
    }
}
