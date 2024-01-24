package thesis.rommler.federation_controller.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import thesis.rommler.federation_controller.api.service.LoginService;

@RestController
public class LoginController {

    private LoginService loginService;

    @Autowired
    public LoginController(LoginService loginService){
        this.loginService = loginService;
    }

    @GetMapping("/login")
    public String Login(@RequestParam String requester_ip, @RequestParam int requester_port, @RequestParam int socket_port){
        if(loginService.LogInNewConnector(requester_ip, requester_port, socket_port))
            return "ok";
        return "error";
    }

    @GetMapping("/ping")
    public String Ping(){
        return "{\"message\": \"pong\"}";
    }
}
