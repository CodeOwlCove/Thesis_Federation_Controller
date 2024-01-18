package thesis.rommler.federation_controller.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import thesis.rommler.federation_controller.api.answerClasses.GetFilesAnswer;
import thesis.rommler.federation_controller.api.service.FileTransferService;

@RestController
public class Orchestrator {

    private FileTransferService fileTransferService;

    @Autowired
    public Orchestrator(FileTransferService fileTransferService){
        this.fileTransferService = fileTransferService;
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

    @GetMapping("/Ping")
    public String Ping(){
        return "pong";
    }

}
