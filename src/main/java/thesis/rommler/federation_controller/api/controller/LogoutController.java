package thesis.rommler.federation_controller.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import thesis.rommler.federation_controller.api.service.ConnectionService;
import thesis.rommler.federation_controller.api.service.FileInformationService;

@RestController
public class LogoutController {
    private static final Logger logger = LoggerFactory.getLogger(LogoutController.class);

    private ConnectionService connectionService;

    public LogoutController(ConnectionService connectionService){
        this.connectionService = connectionService;
    }

    @GetMapping("/logout")
    public String Logout(@RequestParam String requester_ip, @RequestParam String requester_port){

        if(!connectionService.LogOutConnection(requester_ip, Integer.parseInt(requester_port))){
            logger.error("Error while logging out... Connection not found [IP: " + requester_ip + " Port: " + requester_port + "]");
            return "not found";
        }

        return "ok";
    }

}
