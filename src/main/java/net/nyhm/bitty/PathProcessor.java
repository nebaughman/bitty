package net.nyhm.bitty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class PathProcessor implements ServerLogic
{
    private static final Logger log = LoggerFactory.getLogger(PathProcessor.class);

    private final Map<Path,ServerLogic> mLogic = new HashMap<>();

    public PathProcessor()
    {
    }

    public void add(PathHandler handler)
    {
        add(handler.getPath(), handler);
    }

    public void add(Path path, ServerLogic logic)
    {
        mLogic.put(path, logic);
    }

    @Override
    public void processRequest(ClientRequest request, ServerResponse response)
        throws Exception
    {
        ServerLogic logic = mLogic.get(request.getPath());
        if (logic != null)
        {
            logic.processRequest(request, response);
        }
        else
        {
            log.warn("Unknown path {}", request.getPath());
            response.respond("Unknown path", false);
        }
    }
}
