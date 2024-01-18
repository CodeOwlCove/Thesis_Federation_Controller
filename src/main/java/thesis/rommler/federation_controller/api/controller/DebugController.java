package thesis.rommler.federation_controller.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import thesis.rommler.federation_controller.api.service.FileTransferService;

@RestController
public class DebugController {

    private FileTransferService fileTransferService;

    @Autowired
    public DebugController(FileTransferService fileTransferService){
        this.fileTransferService = fileTransferService;
    }

    @GetMapping("/debug1")
    public void Debug1(){

    }

    @GetMapping("/debug2")
    public void Debug2(){

    }

    @GetMapping("/debug3")
    public void Debug3(){

    }

}
