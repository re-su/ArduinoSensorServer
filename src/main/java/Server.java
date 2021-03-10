import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Server {
    // Dodaj metode OPTIONS pozniej!!
    private static final int PORT = 8000;
    private static final int BACKLOG = 1000;

    private static final String HEADER_ALLOW = "Allow";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final int STATUS_OK = 200;
    private static final int STATUS_METHOD_NOT_ALLOWED = 405;

    private static final int NO_RESPONSE_LENGTH = -1;

    private static final String METHOD_GET = "GET";
    private static final String ALLOWED_METHODS = METHOD_GET;
    private static ReadSerialPort readingPorts;
    protected static final Logger log = Logger.getLogger(Server.class.getName());

    public static void main(final String... args) throws IOException {
        final HttpServer server = HttpServer.create(new InetSocketAddress(PORT), BACKLOG);
        readingPorts = new ReadSerialPort(); //Trzeba odczytywac z portu wczesniej, bo inaczej jest problem
        log.log(Level.INFO, "Server started");
        server.createContext("/getsensordata", data -> {
            try {
                final Headers headers = data.getResponseHeaders();
                final String requestMethod = data.getRequestMethod().toUpperCase();
                switch (requestMethod) {
                    case METHOD_GET:
                        final String responseBody = readingPorts.getData();
                        headers.set(HEADER_CONTENT_TYPE, String.format("application/json; charset=%s", CHARSET));
                        headers.add("Access-Control-Allow-Origin", "*");
                        headers.add("Access-Control-Allow-Credentials", "true"); // zeby bylo mozliwe pobieranie miedzy serwisami
                        final byte[] rawResponseBody = responseBody.getBytes(CHARSET);
                        data.sendResponseHeaders(STATUS_OK, rawResponseBody.length);
                        data.getResponseBody().write(rawResponseBody);
                        // Info
                        log.log(Level.INFO, "Client: {0}", data.getRemoteAddress().getAddress());
                        log.log(Level.INFO, responseBody);
                        break;
                    default:
                        headers.set(HEADER_ALLOW, ALLOWED_METHODS);
                        data.sendResponseHeaders(STATUS_METHOD_NOT_ALLOWED, NO_RESPONSE_LENGTH);
                        break;
                }
            } finally {
                data.close();
            }
        });
        server.start();
    }
}