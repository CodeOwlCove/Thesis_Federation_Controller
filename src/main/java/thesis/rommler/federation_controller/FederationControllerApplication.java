package thesis.rommler.federation_controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
public class FederationControllerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FederationControllerApplication.class, args);
	}

}
