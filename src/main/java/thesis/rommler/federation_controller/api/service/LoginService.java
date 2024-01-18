package thesis.rommler.federation_controller.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import thesis.rommler.federation_controller.api.DataClasses.ConnectionData;
import thesis.rommler.federation_controller.api.answerClasses.GetFilesAnswer;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.lang.System.exit;

@Service
public class LoginService {

    public ArrayList<ConnectionData> activeConnections;
    private final RestTemplate restTemplate;
    ScheduledExecutorService scheduledExecutorService;
    private static final Logger logger = Logger.getLogger(LoginService.class.getName());;

    @Autowired
    public LoginService(RestTemplate restTemplate){
        activeConnections = new ArrayList<>();
        this.restTemplate = restTemplate;

        // Create a ScheduledExecutorService
        scheduledExecutorService = Executors.newScheduledThreadPool(1);


        StartCheckActiveConnections();
    }


    public boolean LogInNewConnector(String requester_ip, int requester_port, int socket_port){
        try {
            activeConnections.add(new ConnectionData(requester_ip, requester_port, socket_port));
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
        scheduledExecutorService.scheduleAtFixedRate(this::CheckActiveConnections, 0, 1, TimeUnit.SECONDS);
    }

    public void StopCheckActiveConnections(){
        System.out.println("Stopping the scheduled executor service (CheckActiveConnections)...");
        scheduledExecutorService.shutdown();
    }

    public void CheckActiveConnections() {
        if(activeConnections.size() <= 0) {
            logger.log(java.util.logging.Level.INFO, "No active connections!");
            return;
        }

        for(var item : activeConnections){
            //Send Ping request
            //If no response, remove from list
            String apiUrl = "http://" + item.requester_ip + ":" + item.requester_port + "/ping";

            try {

                // Make a GET request and handle the response
                String response = restTemplate.getForObject(apiUrl, String.class);

                if (response.equals("pong")) {
                    logger.info("Connection on " + item.requester_ip + ":" + item.requester_port + " is still online!");
                }else{
                    logger.info("Answer on "+item.requester_ip+":"+item.requester_port+" was [" + response + "] - removing from active connections!");
                    activeConnections.remove(item);
                }

            } catch (Exception e){
                logger.severe("Connection on " + item.requester_ip + ":" + item.requester_port + " error!" + e.getMessage());
                activeConnections.remove(item);
            }

        }
    }

    public void LogOutConnector(String requester_ip, int requester_port, int socket_port){
        for(var item : activeConnections){
            if(item.requester_ip.equals(requester_ip) && item.requester_port == requester_port && item.socket_port == socket_port){
                activeConnections.remove(item);
                return;
            }
        }
    }
}
