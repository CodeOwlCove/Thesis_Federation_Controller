package thesis.rommler.federation_controller.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import thesis.rommler.federation_controller.api.service.FileCollectorService;
import thesis.rommler.federation_controller.api.service.FileTransferService;
import thesis.rommler.federation_controller.api.service.LoginService;

@RestController
public class DebugController {

    private FileTransferService fileTransferService;
    private LoginService loginService;
    private FileCollectorService fileCollectorService;
    private static final Logger logger = LoggerFactory.getLogger(DebugController.class);


    @Autowired
    public DebugController(FileTransferService fileTransferService, LoginService loginService, FileCollectorService fileCollectorService){
        this.fileTransferService = fileTransferService;
        this.loginService = loginService;
        this.fileCollectorService = fileCollectorService;
    }

    @GetMapping("/debug1")
    public String Debug1(){
        //Collect files from all connected Backend Clients
        System.out.println("Starting file transfer");
        try {
            fileCollectorService.HandleCollectionProcesses(loginService.activeConnections);
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
            fileCollectorService.RezipReceivedFiles();
        }catch (Exception e){
            System.out.println("Error while re-zipping files: " + e.getMessage());
        }
    }

    @GetMapping("/debug3")
    public void Debug3(){

    }

}
