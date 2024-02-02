package thesis.rommler.federation_controller.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import thesis.rommler.federation_controller.api.DataClasses.ConnectionData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@Service
public class FileCollectorService {

    /**
     * Thread for collecting files from a single participant
     */
    public class FileCollectionThread extends Thread{
        int participantIndex;
        ConnectionData connection;
        public FileCollectionThread (int participantIndex, ConnectionData connection){
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
    private final RestTemplate restTemplate;


    public FileCollectorService(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
        sockets = new ArrayList<>();
        collectionThreads = new ArrayList<>();
    }

    /**
     * Opens a socket for each participant and creates a thread for each socket
     * @param activeConnections The list of active connections
     */
    private void OpenSockets(ArrayList<ConnectionData> activeConnections){
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
                    collectionThreads.add(new FileCollectionThread(participantIndex, connection));
                    participantIndex++;
                }
            }catch  (Exception e) {
                logger.error("Error while creating socket on " + connection.requester_ip + ":" + connection.socket_port + " [Error: " + e.getMessage() + "]");
            }
        }

        logger.info("- " + sockets.size() + " sockets opened.");
    }

    /**
     * Requests a socket from a connector on the specified port
     * @param connection The connection data of the connector
     * @param socketPort The port of the socket
     * @return True if the socket was started successfully
     */
    private boolean RequestSocketFromConnector(ConnectionData connection, int socketPort){
        String apiUrl = "http://" + connection.requester_ip + ":" + connection.requester_REST_port + "/StartSocket?socket_port=" + socketPort;

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
        //Open Sockets and create Threads
        OpenSockets(activeConnections);

        logger.info(activeConnections.toString());
        logger.info(collectionThreads.toString());

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
    }

    /**
     * Checks if a byte array ends with a specified suffix
     * @param data The byte array
     * @param length The length of the byte array
     * @param suffix The suffix
     * @return True if the byte array ends with the suffix
     */
    private boolean endsWith(byte[] data, int length, byte[] suffix) {
        if (length < suffix.length) {
            return false;
        }
        for (int i = 0; i < suffix.length; i++) {
            if (data[length - suffix.length + i] != suffix[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Collects the files from a single participant
     * @param participantSocket The socket of the participant
     * @param collection_id The id of the collection
     */
    private void CollectFiles(Socket participantSocket, int collection_id, ConnectionData connection){
        try {
            logger.info("- Collecting files from " + participantSocket.getInetAddress().toString() + ":" + participantSocket.getPort() + "...");
            InputStream inputStream = participantSocket.getInputStream();
            String outputPath = "F:\\Masterarbeit_Gits\\federation_controller\\src\\main\\resources\\Incomming\\incomming_" + collection_id + ".zip";

            //Delete Old File
            if(new File(outputPath).exists()){
                new File(outputPath).delete();
            }

            // Read file content
            FileOutputStream fileOutputStream = new FileOutputStream(outputPath);
            byte[] buffer = new byte[1024];
            int bytesRead;
            int totalBytesRead = 0;
            int threshold = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {

                /*

                // Check for the end of the file content using a delimiter
                String data = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                if (data.contains("EOF")) {
                    break; // Stop reading when "END_OF_FILE" is received
                }
                 */

                // Define the end-of-file marker as a byte array
                byte[] eofMarker = "End_Of_File".getBytes(StandardCharsets.UTF_8);
                if (endsWith(buffer, bytesRead, eofMarker)) {
                    break; // Stop reading when the end-of-file marker is received
                }

                fileOutputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;

                if(totalBytesRead > threshold){
                    threshold += 1000000;
                    logger.info("Progress: " + totalBytesRead + " MB read from [" + participantSocket.getInetAddress().toString() + ":" + participantSocket.getPort() + "].");
                }
            }
            logger.info("Bytes read: " + totalBytesRead);

            fileOutputStream.close();
            logger.info("File received: F:\\Masterarbeit_Gits\\federation_controller\\Asset_Output\\output_" + collection_id + ".zip");

            if(RequestCloseSocket(connection, connection.socket_port)){
                logger.info("- Socket closed on " + connection.requester_ip + ":" + connection.socket_port + ".");
            }else{
                logger.info("- Socket could not be closed on " + connection.requester_ip + ":" + connection.socket_port + ".");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean RequestCloseSocket(ConnectionData connection, int socketPort){
        logger.info("- Requesting socket close on " + connection.requester_ip + ":" + socketPort + ".");
        String apiUrl = "http://" + connection.requester_ip + ":" + connection.requester_REST_port + "/CloseSocket?socket_port=" + socketPort;

        try {
            // Make a GET request and handle the response
            String response = restTemplate.getForObject(apiUrl, String.class);

            if (response.equals("socket_closed")) {
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
}
