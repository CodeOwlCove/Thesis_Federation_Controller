package thesis.rommler.federation_controller.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import thesis.rommler.federation_controller.api.answerClasses.GetFilesAnswer;
import thesis.rommler.federation_controller.api.service.FileTransferService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    @GetMapping("/GetFilesFrontend")
    public void getFilesFrontend(HttpServletResponse response) throws IOException {
        String fileLocation="F:\\Masterarbeit_Gits\\federation_controller\\Assets\\Outgoing\\Outgoing.rar";

        File downloadFile= new File(fileLocation);

        byte[] isr = Files.readAllBytes(downloadFile.toPath());
        ByteArrayOutputStream out = new ByteArrayOutputStream(isr.length);
        out.write(isr, 0, isr.length);

        response.setContentType("application/rar");
        // Use 'inline' for preview and 'attachement' for download in browser.
        response.addHeader("Content-Disposition", "inline; filename=" + "Outgoing.rar");

        OutputStream os;
        try {
            os = response.getOutputStream();
            out.writeTo(os);
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/Ping")
    public String Ping(){
        return "pong";
    }

}
