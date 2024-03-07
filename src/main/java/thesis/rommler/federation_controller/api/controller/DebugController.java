package thesis.rommler.federation_controller.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import thesis.rommler.federation_controller.api.service.FileCollector.AllFileCollectorService;
import thesis.rommler.federation_controller.api.service.FileTransferService;
import thesis.rommler.federation_controller.api.service.ConnectionService;

@RestController
public class DebugController {

    private FileTransferService fileTransferService;
    private ConnectionService connectionService;
    private AllFileCollectorService allFileCollectorService;
    private static final Logger logger = LoggerFactory.getLogger(DebugController.class);


    @Autowired
    public DebugController(FileTransferService fileTransferService, ConnectionService connectionService, AllFileCollectorService allFileCollectorService){
        this.fileTransferService = fileTransferService;
        this.connectionService = connectionService;
        this.allFileCollectorService = allFileCollectorService;
    }

    @GetMapping("/debug1")
    public String Debug1(){
        //Collect files from all connected Backend Clients
        System.out.println("Starting file transfer");
        try {
            allFileCollectorService.HandleCollectionProcesses(connectionService.activeConnections);
            logger.info("- File collection finished...");
            return "OK...";
        }catch (Exception e){
            logger.error("Error while collecting files: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/debug2")
    public void Debug2(){
        try {
            allFileCollectorService.RezipReceivedFiles();
        }catch (Exception e){
            System.out.println("Error while re-zipping files: " + e.getMessage());
        }
    }

    @GetMapping("/debug3")
    public void Debug3(){

    }

}
