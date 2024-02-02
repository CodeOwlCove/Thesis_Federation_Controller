package thesis.rommler.federation_controller.api.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import thesis.rommler.federation_controller.api.DataClasses.ConnectionData;
import thesis.rommler.federation_controller.api.answerClasses.FileInformation;

import java.util.ArrayList;

@Service
public class FileInformationService {

    private final LoginService loginService;
    private final RestTemplate restTemplate;

    public FileInformationService(LoginService loginService, RestTemplate restTemplate){
        this.loginService = loginService;
        this.restTemplate = restTemplate;
    }

    public ArrayList<FileInformation> CollectFileInformation() {
        ArrayList<FileInformation> fileInformationList = new ArrayList<>();

        for (ConnectionData data : loginService.activeConnections){
            String apiUrl = "http://" + data.requester_ip + ":" + data.requester_REST_port + "/GetFileInformation";

            try {

                // Make a GET request and handle the response
                ResponseEntity<ArrayList<FileInformation>> responseEntity = restTemplate.exchange(
                        apiUrl,
                        org.springframework.http.HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<ArrayList<FileInformation>>() {}
                );

                ArrayList<FileInformation> result = responseEntity.getBody();

                // If the result is null or empty, continue with the next connection
                if(result == null || result.size() == 0)
                    continue;

                // Add the result to the list
                for(FileInformation file : result){
                    fileInformationList.add(file);
                }


            } catch (Exception e) {
                System.out.println("Error while collecting file information from " + data.requester_ip + ":" + data.requester_REST_port + ": " + e.getMessage());
            }

        }



        return fileInformationList;
    }
}
