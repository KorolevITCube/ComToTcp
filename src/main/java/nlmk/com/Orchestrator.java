package nlmk.com;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Orchestrator {
    private static final Logger log = LoggerFactory.getLogger(Orchestrator.class);
    // Очередь для хранения запросов, которые нужно выполнить
    private ConcurrentLinkedQueue<RequestWrapper> requestsQueue;

    private static Orchestrator instance;

    public static Orchestrator getInstance(){
        if(instance == null){
            instance = new Orchestrator();
        }
        return instance;
    }

    private ComDriver driver;

    public void addRequestToQueue(RequestWrapper request) {
        requestsQueue.add(request);
    }

    public Orchestrator() {
        requestsQueue = new ConcurrentLinkedQueue<>();
        this.driver = ComDriver.getInstance();
        Thread daemon = new Thread(new RequestsConsumer());
        daemon.setDaemon(true);
        daemon.start();
    }

    private class RequestsConsumer implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!requestsQueue.isEmpty()) {
                    RequestWrapper wrapper = null;
                    try {
                        log.info("New iteration in queue requests. Request count is: " + requestsQueue.size());
                        while (!requestsQueue.isEmpty()) {
                            wrapper = requestsQueue.poll();
                            byte[] request = wrapper.getRequest();
                            driver.writeRequest(request);
                            byte[] currentResponse = driver.readResponse();
                            wrapper.getControllerLink().setResponse(currentResponse);
                        }
                    } catch (Exception e) {
                        log.error("Work job is fall\n\trequest is: "+Util.convertBytesToString(wrapper.getRequest()));
                        log.error(e.getLocalizedMessage());
                        log.error(e.getMessage());
                        wrapper.getControllerLink().setResponse(new byte[10]);
                    }
                }
            }
        }
    }

}
