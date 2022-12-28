package nlmk.com;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController {
    static ExecutorService executeIt = Executors.newFixedThreadPool(12);
    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    public static void main(String[] args) {

        try (ServerSocket server = new ServerSocket(9876)) {
            log.info("Main Controller was created, ready to connect");

            while (!server.isClosed()) {
                Socket client = server.accept();
                executeIt.execute(new TcpController(client));
                System.out.print("Connection accepted.");
            }

            // закрытие пула нитей после завершения работы всех нитей
            executeIt.shutdown();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}
