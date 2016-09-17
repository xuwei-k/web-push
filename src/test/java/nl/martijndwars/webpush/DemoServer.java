package nl.martijndwars.webpush;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

public class DemoServer {
    public DemoServer(int port) throws Exception {
        Server server = new Server(port);

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setWelcomeFiles(new String[]{"src/test/resources/demo/index.html"});
        resourceHandler.setResourceBase("src/test/resources/demo");

        HandlerList handlerList = new HandlerList();
        handlerList.setHandlers(new Handler[]{resourceHandler, new DefaultHandler()});

        server.setHandler(handlerList);

        server.start();
    }
}
