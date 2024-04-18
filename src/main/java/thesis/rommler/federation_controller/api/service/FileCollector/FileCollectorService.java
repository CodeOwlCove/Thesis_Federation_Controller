package thesis.rommler.federation_controller.api.service.FileCollector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import thesis.rommler.federation_controller.api.DataClasses.ConnectionData;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileCollectorService {


    protected final String incomingFolder = "src/main/resources/Incoming";
    protected final String outgoingFolder = "src/main/resources/Outgoing";
    private static final Logger logger = LoggerFactory.getLogger(FileCollectorService.class);
    protected final RestTemplate restTemplate;

    public FileCollectorService(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }



    protected void CheckForFolders(){
        logger.info("- Checking for folders...");
        Path path = Paths.get(incomingFolder);
        if (!Files.isDirectory(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        path = Paths.get(outgoingFolder);
        if (!Files.isDirectory(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Deletes old zip files from the incoming and outgoing folders
     */
    public void DeleteOldZipFiles(){
        logger.info("- Deleting old zip files...");

        //Delete Old Files
        File incomingFolderFile = new File(incomingFolder);
        File[] incomingFiles = incomingFolderFile.listFiles();
        for (File file : incomingFiles) {
            if (!file.isDirectory()) {
                file.delete();
            }
        }

        File outgoingFolderFile = new File(outgoingFolder);
        File[] outgoingFiles = outgoingFolderFile.listFiles();
        for (File file : outgoingFiles) {
            if (!file.isDirectory()) {
                file.delete();
            }
        }
    }

    public void RezipReceivedFiles(){
        // Specify the name of the output zip file
        String zipOutputFileName = "Outgoing.zip";

        // Check if the folder exists
        Path path = Paths.get(incomingFolder);

        try {
            zipFolderContents(incomingFolder, outgoingFolder, zipOutputFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Zips the contents of a folder
     * @param sourceFolder The folder to be zipped
     * @param outputFolderPath The folder where the zip file will be saved
     * @param zipFileName The name of the zip file
     */
    public static void zipFolderContents(String sourceFolder, String outputFolderPath, String zipFileName) {
        try {
            Path outputFolder = Paths.get(outputFolderPath);
            Files.createDirectories(outputFolder);

            File sourceFolderFile = new File(sourceFolder);
            File zipFile = new File(outputFolder.toFile(), zipFileName);

            try (FileOutputStream fos = new FileOutputStream(zipFile);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {
                zipDirectoryContents(sourceFolderFile, zos);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Zips the contents of a folder
     * @param folder The folder to be zipped
     * @param zos The zip output stream
     * @throws IOException
     */
    private static void zipDirectoryContents(File folder, ZipOutputStream zos) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                zipDirectoryContents(file, zos);
                continue;
            }

            FileInputStream fis = new FileInputStream(file);
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zos.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
            }

            fis.close();
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
    void CollectFiles(Socket participantSocket, int collection_id, ConnectionData connection){
    try {
        logger.info("- Collecting files from " + participantSocket.getInetAddress().toString() + ":" + participantSocket.getPort() + "...");
        InputStream inputStream = participantSocket.getInputStream();

        String outputPath = Paths.get(incomingFolder, "incoming_" + collection_id + ".zip").toString();

        //Delete Old File
        if(new File(outputPath).exists()){
            new File(outputPath).delete();
        }

        // Read file content
        FileOutputStream fileOutputStream = new FileOutputStream(outputPath);
        byte[] buffer = new byte[1024];
        int bytesRead;
        int totalBytesRead = 0;
        int chunksRead = 0;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            totalBytesRead += bytesRead;

            fileOutputStream.write(buffer, 0, bytesRead);

            // Define the end-of-file marker as a byte array
            byte[] eofMarker = "End_Of_File".getBytes(StandardCharsets.UTF_8);
            if (endsWith(buffer, bytesRead, eofMarker)) {
                break; // Stop reading when the end-of-file marker is received
            }

            if(chunksRead >= 10000){
                chunksRead= 0;
                logger.info("Progress: " + totalBytesRead + " Bytes read from [" + participantSocket.getInetAddress().toString() + ":" + participantSocket.getPort() + "].");
            }
            chunksRead++;
        }

        inputStream.close();
        fileOutputStream.close();

        logger.info("File received: " + incomingFolder + collection_id + ".zip");

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
