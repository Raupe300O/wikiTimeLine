package basecamp.project.server.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@EnableAutoConfiguration
public class IndexController {

	@Value("${service.key}")
	private String key;

	@RequestMapping(value = { "/", "/index" }, method = RequestMethod.GET)
	public String index(Model model) {

		String message = "Our model message.";
		model.addAttribute("message", message);

		model.addAttribute("key", key);

		return "index";
	}

	@RequestMapping(value = { "/hello" }, method = RequestMethod.GET)
	public String hello(Model model, @RequestParam(defaultValue = "User") String name) {

		model.addAttribute("name", name);

		return "hello";
	}


	@RequestMapping(value = { "/hello2" }, method = RequestMethod.GET)
	public String helloAgain(Model model, @RequestParam(name = "name", defaultValue = "User") String n) {

		model.addAttribute("name", n);

		return "hello";
	}


}
