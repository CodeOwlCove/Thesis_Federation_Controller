package thesis.rommler.federation_controller.api.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import thesis.rommler.federation_controller.api.service.FileInformationService;

@RestController
public class FileInformationController {

    private static final Logger logger = LoggerFactory.getLogger(FileInformationController.class);

    private final FileInformationService fileInformationService;

    public FileInformationController(FileInformationService fileInformationService){
        this.fileInformationService = fileInformationService;
    }

    @GetMapping("/GetFileInformation")
    public String getFileInformation() {
        logger.info("GetFileInformation request received");
        var fileInformation = fileInformationService.CollectFileInformation();

        Gson gson = new GsonBuilder().create();
        String jsonString = gson.toJson(fileInformation);

        return jsonString;
    }

}