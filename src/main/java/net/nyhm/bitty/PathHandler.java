package net.nyhm.bitty;

import java.nio.file.Path;

/**
 * This {@link ServerLogic} implementation contains a fixed path that it will process. Implementations
 * must override the {@link #processRequest(ClientRequest, ServerResponse)} method. That method shall
 * only be called with requests to the specified path. Consider using PathHandler with a {@link PathProcessor}.
 */
public abstract class PathHandler implements ServerLogic
{
    private final Path mPath;

    public PathHandler(Path path)
    {
        mPath = path;
    }

    public Path getPath()
    {
        return mPath;
    }
}
