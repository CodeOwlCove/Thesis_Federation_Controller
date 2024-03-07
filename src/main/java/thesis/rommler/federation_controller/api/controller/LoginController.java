package thesis.rommler.federation_controller.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import thesis.rommler.federation_controller.api.service.ConnectionService;

@RestController
public class LoginController {

    private ConnectionService connectionService;

    @Autowired
    public LoginController(ConnectionService connectionService){
        this.connectionService = connectionService;
    }

    @GetMapping("/login")
    public String Login(@RequestParam String requester_ip, @RequestParam int requester_port, @RequestParam int socket_port){
        System.out.println("Login request received from: " + requester_ip + ":" + requester_port + " on port: " + socket_port);
        if(connectionService.LogInNewConnector(requester_ip, requester_port, socket_port))
            return "ok";
        return "error";
    }

    @GetMapping("/ping")
    public String Ping(){
        return "{\"message\": \"pong\"}";
    }
}
