package thesis.rommler.federation_controller.api.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import thesis.rommler.federation_controller.api.service.FileInformationService;
import thesis.rommler.federation_controller.api.service.LoginService;

@RestController
public class FileInformationController {

    private final FileInformationService fileInformationService;

    public FileInformationController(FileInformationService fileInformationService){
        this.fileInformationService = fileInformationService;
    }

    @GetMapping("/GetFileInformation")
    public String getFileInformation() {
        var fileInformation = fileInformationService.CollectFileInformation();

        Gson gson = new GsonBuilder().create();
        String jsonString = gson.toJson(fileInformation);

        return jsonString;
    }

}