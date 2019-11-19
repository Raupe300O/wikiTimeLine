package basecamp.project.server;

import basecamp.project.storage.StorageProperties;
import basecamp.project.storage.StorageService;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@EnableConfigurationProperties(StorageProperties.class)
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



	// call http://<host>/hello.json
	@RequestMapping(value = "/hello", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public Object helloRest() {
		JSONObject j = new JSONObject();
		j.put("hello", "world");
		return j;
	}

	// call http://<host>/hello.html or just http://<host>/hello
	@RequestMapping(value = "/hello", method = RequestMethod.GET)
	public String helloHtml(Model model) {
		model.addAttribute("message", "hello world");
		return "hello";
	}





	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
	}


	@Bean
	CommandLineRunner init(StorageService storageService) {
		return (args) -> {
			storageService.deleteAll();
			storageService.init();
		};
	}

}
