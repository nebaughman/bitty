package net.nyhm.bitty;

/**
 * Client requests are handed off to server logic to be processed.
 */
public interface ServerLogic
{
    /**
     * Implementations call {@link ServerResponse#respond(String)} to send a response. The call does not
     * need to (and should not) block. The response may be made asynchronously.
     */
    void processRequest(ClientRequest request, ServerResponse response) throws Exception;
}
