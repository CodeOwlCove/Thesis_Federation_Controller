package thesis.rommler.federation_controller.api.service.FileCollector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import thesis.rommler.federation_controller.api.DataClasses.ConnectionData;
import thesis.rommler.federation_controller.api.service.FileTransferService;

import java.net.Socket;
import java.util.ArrayList;

@Service
public class SelectedFileCollectorService extends FileCollectorService{

    /**
     * Thread for collecting files from a single participant
     */
    private class SelectedFileCollectionThread extends Thread{
        int participantIndex;
        ConnectionData connection;
        public SelectedFileCollectionThread(int participantIndex, ConnectionData connection){
            this.participantIndex = participantIndex;
            this.connection = connection;
        }
        @Override
        public void run() {
            CollectFiles(sockets.get(participantIndex), participantIndex, connection);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(FileTransferService.class);
    private ArrayList<Socket> sockets;
    private ArrayList<Thread> collectionThreads;

    public SelectedFileCollectorService(RestTemplate restTemplate){
        super(restTemplate);
        collectionThreads = new ArrayList<>();
    }

    private void OpenSockets(ArrayList<ConnectionData> activeConnections, String[] fileNames){
        //Reset Sockets and Threads
        sockets = new ArrayList<>();
        collectionThreads = new ArrayList<>();

        int participantIndex = 0;

        if(activeConnections.isEmpty()){
            logger.info("- No active connections.");
            return;
        }

        for(var connection : activeConnections){
            try {
                //Request Socket from Connector
                if(RequestSocketFromConnector(connection, 9900 + participantIndex, fileNames)){
                    connection.socket_port = 9900 + participantIndex;
                    logger.info("- Socket opened on " + connection.requester_ip + ":" + connection.socket_port + ".");

                    //Create Socket
                    sockets.add(new Socket(connection.requester_ip, connection.socket_port));
                    //sockets.get(participantIndex).setSoTimeout(5000);
                    //Create Thread
                    collectionThreads.add(new SelectedFileCollectorService.SelectedFileCollectionThread(participantIndex, connection));
                    participantIndex++;
                }
            }catch  (Exception e) {
                logger.error("Error while creating socket on " + connection.requester_ip + ":" + connection.socket_port + " [Error: " + e.getMessage() + "]");
            }
        }

        logger.info("- " + sockets.size() + " socket(s) opened.");
    }

    private boolean RequestSocketFromConnector(ConnectionData connection, int socketPort, String[] fileNames){
        var restFileArrayString = "";
        for(var file : fileNames){
            restFileArrayString += "file_name=" + file + "&";
        }
        //Remove last "&"
        restFileArrayString = restFileArrayString.substring(0, restFileArrayString.length() - 1);

        String apiUrl = "http://" + connection.requester_ip + ":" + connection.requester_REST_port + "/StartSelectedFilesSocket?socket_port=" + socketPort + "&" + restFileArrayString;

        try {
            // Make a GET request and handle the response
            String response = restTemplate.getForObject(apiUrl, String.class);

            if (response.equals("socket_started")) {
                logger.info("- Socket started on " + connection.requester_ip + ":" + socketPort + ".");
                return true;
            } else {
                logger.info("- Socket could not be started on " + connection.requester_ip + ":" + socketPort + ".");
                return false;
            }

        } catch (Exception e) {
            logger.error("Error while requesting socket on " + connection.requester_ip + ":" + socketPort + " [Error: " + e.getMessage() + "]");
            return false;
        }
    }

    public void HandleCollectionProcesses(ArrayList<ConnectionData> activeConnections, String[] fileNames){
        logger.info("- Starting file collection process...");

        CheckForFolders();

        //Open Sockets and create Threads
        OpenSockets(activeConnections, fileNames);

        logger.info("- Starting file collection threads...");

        //Start all threads
        for(var thread : collectionThreads){
            thread.start();
        }

        logger.info("- Waiting for file collection threads to finish...");

        //Wait for all threads to finish
        for(var thread : collectionThreads){
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        RezipReceivedFiles();
    }
}
