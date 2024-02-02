package thesis.rommler.federation_controller.api.controller;

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

    @Autowired
    public DebugController(FileTransferService fileTransferService, LoginService loginService, FileCollectorService fileCollectorService){
        this.fileTransferService = fileTransferService;
        this.loginService = loginService;
        this.fileCollectorService = fileCollectorService;
    }

    @GetMapping("/debug1")
    public String Debug1(){
        System.out.println("Starting file transfer");
        try {
            fileCollectorService.HandleCollectionProcesses(loginService.activeConnections);
            System.out.println("File transfer finished");
            return "Files collected!";
        }catch (Exception e){
            return("Error while collecting files: " + e.getMessage());
        }

    }

    @GetMapping("/debug2")
    public void Debug2(){

    }

    @GetMapping("/debug3")
    public void Debug3(){

    }

}
