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
public class AllFileCollectorService extends FileCollectorService{

    /**
     * Thread for collecting files from a single participant
     */
    private class AllFileCollectionThread extends Thread{
        int participantIndex;
        ConnectionData connection;
        public AllFileCollectionThread(int participantIndex, ConnectionData connection){
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

    public AllFileCollectorService(RestTemplate restTemplate){
        super(restTemplate);
        collectionThreads = new ArrayList<>();
    }

    /**
     * Opens a socket for each participant and creates a thread for each socket
     * @param activeConnections The list of active connections
     */
    private void OpenSockets(ArrayList<ConnectionData> activeConnections){
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
                if(RequestSocketFromConnector(connection, 9900 + participantIndex)){
                    connection.socket_port = 9900 + participantIndex;
                    logger.info("- Socket opened on " + connection.requester_ip + ":" + connection.socket_port + ".");

                    //Create Socket
                    sockets.add(new Socket(connection.requester_ip, connection.socket_port));
                    //sockets.get(participantIndex).setSoTimeout(5000);
                    //Create Thread
                    collectionThreads.add(new AllFileCollectionThread(participantIndex, connection));
                    participantIndex++;
                }
            }catch  (Exception e) {
                logger.error("Error while creating socket on " + connection.requester_ip + ":" + connection.socket_port + " [Error: " + e.getMessage() + "]");
            }
        }

        logger.info("- " + sockets.size() + " socket(s) opened.");
    }

    /**
     * Requests a socket from a connector on the specified port
     * @param connection The connection data of the connector
     * @param socketPort The port of the socket
     * @return True if the socket was started successfully
     */
    private boolean RequestSocketFromConnector(ConnectionData connection, int socketPort){
        String apiUrl = "http://" + connection.requester_ip + ":" + connection.requester_REST_port + "/StartAllFilesSocket?socket_port=" + socketPort;

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

    /**
     * Handles the collection of files from all participants
     * @param activeConnections The list of active connections
     */
    public void HandleCollectionProcesses(ArrayList<ConnectionData> activeConnections){
        logger.info("- Starting file collection process...");

        CheckForFolders();

        //Open Sockets and create Threads
        OpenSockets(activeConnections);

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
