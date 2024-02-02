package thesis.rommler.federation_controller.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import thesis.rommler.federation_controller.api.DataClasses.ConnectionData;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
public class LoginService {

    public ArrayList<ConnectionData> activeConnections;
    private final RestTemplate restTemplate;
    ScheduledExecutorService scheduledExecutorService;
    private static final Logger logger = Logger.getLogger(LoginService.class.getName());

    private int retryDelay = 5;
    private int initialCheckDelay = 0;

    @Autowired
    public LoginService(RestTemplate restTemplate){
        activeConnections = new ArrayList<>();
        this.restTemplate = restTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {
        logger.info("- Application started.");

        // Create a ScheduledExecutorService
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        StartCheckActiveConnections();
    }


    /**
     * Logs in a new connector
     * @param requester_ip The IP of the connector
     * @param requester_port The port of the connector
     * @param socket_port The port of the socket
     * @return
     */
    public boolean LogInNewConnector(String requester_ip, int requester_port, int socket_port){
        try {
            activeConnections.add(new ConnectionData(requester_ip, requester_port, socket_port));
            logger.info("- New connection logged on: " + requester_ip + ":" + requester_port + ".");
            return true;
        } catch (Exception e){
            return false;
        }
    }

    public void StartCheckActiveConnections(){
        // Register a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // This code will be executed when the program is stopping
            StopCheckActiveConnections();
        }));

        // Schedule the task to run every 10 seconds
        scheduledExecutorService.scheduleAtFixedRate(this::CheckActiveConnections, initialCheckDelay, retryDelay, TimeUnit.SECONDS);
    }

    public void StopCheckActiveConnections(){
        logger.info("- Stopping the scheduled executor service (CheckActiveConnections)...");
        scheduledExecutorService.shutdown();
    }

    public void CheckActiveConnections() {
        if(activeConnections.size() == 0) {
            logger.log(java.util.logging.Level.INFO, "- No active connections!");
            return;
        }

        for (ConnectionData item : activeConnections) {
            //Send Ping request
            //If no response, remove from list
            String apiUrl = "http://" + item.requester_ip + ":" + item.requester_REST_port + "/ping";

            try {

                // Make a GET request and handle the response
                String response = restTemplate.getForObject(apiUrl, String.class);

                if (response.equals("pong")) {
                    logger.info("- Connection on " + item.requester_ip + ":" + item.requester_REST_port + " is still online!");
                } else {
                    logger.info("- Answer on " + item.requester_ip + ":" + item.requester_REST_port + " was [" + response + "] - removing from active connections!");
                    activeConnections.remove(item);
                }

            } catch (Exception e) {
                logger.severe("- Connection on " + item.requester_ip + ":" + item.requester_REST_port + " lost! \n" + e.getMessage());
                activeConnections.remove(item);

                // Restart the scheduled executor service
                // If you don't do this, the executor service will not work anymore for some reason
                scheduledExecutorService.scheduleAtFixedRate(this::CheckActiveConnections, initialCheckDelay, retryDelay, TimeUnit.SECONDS);
            }
        }
    }

    public void LogOutConnector(String requester_ip, int requester_port, int socket_port){
        for(var item : activeConnections){
            if(item.requester_ip.equals(requester_ip) && item.requester_REST_port == requester_port && item.socket_port == socket_port){
                activeConnections.remove(item);
                return;
            }
        }
    }
}
