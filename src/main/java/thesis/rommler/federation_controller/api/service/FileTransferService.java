package thesis.rommler.federation_controller.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Service
public class FileTransferService {

    private static final Logger logger = LoggerFactory.getLogger(FileTransferService.class);

    private Socket socket;

    public void TransferFiles(String requestIp, int socketPort) {
        ConnectToSocket(requestIp, socketPort);
    }

    private void CollectFiles(){
        new Thread(() -> {
            try {
                InputStream inputStream = socket.getInputStream();
                String outputPath = "F:\\Masterarbeit_Gits\\federation_controller\\Asset_Output\\output.zip";

                // Read file name
                // byte[] fileNameBytes = new byte[1024];
                // int fileNameLength = inputStream.read(fileNameBytes);
                // String fileName = new String(fileNameBytes, 0, fileNameLength);

                //Delete Old File
                if(new File(outputPath).exists()){
                    new File(outputPath).delete();
                }

                // Read file content
                FileOutputStream fileOutputStream = new FileOutputStream(outputPath);
                byte[] buffer = new byte[1024];
                int bytesRead;
                int totalBytesRead = 0;
                while ((bytesRead = inputStream.read(buffer)) != -1) {

                    // Check for the end of the file content using a delimiter
                    String data = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                    if (data.contains("END_OF_FILE")) {
                        break; // Stop reading when "END_OF_FILE" is received
                    }

                    fileOutputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    System.out.println("Progress: " + totalBytesRead + " bytes read");
                }

                fileOutputStream.close();
                logger.info("File received: F:\\Masterarbeit_Gits\\federation_controller\\Asset_Output\\output.zip");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void ConnectToSocket(String requestIp, int socketPort){
        try {
            socket = new Socket(requestIp, socketPort);
            logger.info("Connected to socket.");
            CollectFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
