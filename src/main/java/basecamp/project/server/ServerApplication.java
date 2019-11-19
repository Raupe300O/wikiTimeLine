package basecamp.project.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@EnableAutoConfiguration
public class ServerApplication {

	@Value("${service.key}")
	private String key;

	@RequestMapping(value = { "/", "/index" }, method = RequestMethod.GET)
	public String index(Model model) {

		String message = "Our model message.";
		model.addAttribute("message", message);

		model.addAttribute("key", key);

		return "index";
	}


	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
	}


}
