package thesis.rommler.federation_controller.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import thesis.rommler.federation_controller.api.answerClasses.GetFilesAnswer;
import thesis.rommler.federation_controller.api.service.*;
import thesis.rommler.federation_controller.api.service.FileCollector.AllFileCollectorService;
import thesis.rommler.federation_controller.api.service.FileCollector.SelectedFileCollectorService;

import java.io.*;
import java.util.Arrays;
import java.util.Map;

import static java.lang.Thread.sleep;

@RestController
public class Orchestrator {

    private static final Logger logger = LoggerFactory.getLogger(Orchestrator.class);
    private FileTransferService fileTransferService;
    private AllFileCollectorService allFileCollectorService;
    private SelectedFileCollectorService selectedFileCollectorService;
    private LoginService loginService;
    private FileSenderService fileSenderService;

    private static final String FILE_PATH = "src/main/resources/Outgoing/Outgoing.zip";


    @Autowired
    public Orchestrator(FileTransferService fileTransferService, AllFileCollectorService allFileCollectorService,
                        LoginService loginService, FileSenderService fileSenderService,
                        SelectedFileCollectorService selectedFileCollectorService){
        this.fileTransferService = fileTransferService;
        this.allFileCollectorService = allFileCollectorService;
        this.loginService = loginService;
        this.fileSenderService = fileSenderService;
        this.selectedFileCollectorService = selectedFileCollectorService;
    }

    @GetMapping("/GetFiles")
    public String getFiles(@RequestParam String request_ip, @RequestParam int socket_port){
        fileTransferService.TransferFiles(request_ip, socket_port);

        GetFilesAnswer getFilesAnswer = new GetFilesAnswer();
        getFilesAnswer.ip = request_ip;
        getFilesAnswer.port = socket_port;

        // Convert Java object to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            return objectMapper.writeValueAsString(getFilesAnswer);
        } catch (Exception e){
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/GetAllFilesFrontend")
    public void getAllFilesFrontend(HttpServletResponse response) throws IOException {
        //Collect files from all connected Backend Clients
        System.out.println("Starting file transfer");
        try {
            allFileCollectorService.HandleCollectionProcesses(loginService.activeConnections);
            logger.info("- File collection finished...");
        }catch (Exception e){
            logger.error("Error while collecting files: " + e.getMessage());
        }

        try {
            fileSenderService.SendAllFilesToFrontend(response);
        } catch (Exception e){
            logger.error("Error while sending files to frontend: " + e.getMessage());
        } finally {
            allFileCollectorService.DeleteOldZipFiles();
        }
    }

    @PostMapping("/GetSelectedFilesFrontend")
    public void getSelectedFilesFrontend(@RequestBody Map<String, String[]> requestBody, HttpServletResponse response){
        String[] selectedFiles = requestBody.get("selectedFiles");

        logger.info("- Selected files: " + Arrays.toString(selectedFiles));

        //Collect files from all connected Backend Clients
        System.out.println("Starting selected file transfer");
        try {
            selectedFileCollectorService.HandleCollectionProcesses(loginService.activeConnections, selectedFiles);
            logger.info("- File collection finished...");
        }catch (Exception e){
            logger.error("Error while collecting files: " + e.getMessage());
        }

        try {
            fileSenderService.SendAllFilesToFrontend(response);
        } catch (Exception e){
            logger.error("Error while sending files to frontend: " + e.getMessage());
        } finally {
            allFileCollectorService.DeleteOldZipFiles();
        }
    }

    @GetMapping("/Ping")
    public String Ping(){
        return "pong";
    }

}
