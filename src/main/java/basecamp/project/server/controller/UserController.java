package basecamp.project.server.controller;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
@RequestMapping("/users")
public class UserController {

	@Value("${service.key}")
	private String key;

	@GetMapping
	public String users() {

		JSONObject result =new JSONObject();
		result.put("message", "OK");

		JSONObject user1 = new JSONObject();
		user1.put("first", "User");
		user1.put("last", "A");

		JSONObject user2 = new JSONObject();
		user2.put("first", "Person");
		user2.put("last", "B");

		JSONArray userList = new JSONArray();
		userList.add(user1);
		userList.add(user2);

		result.put("users", userList);
		return result.toJSONString();
	}


}
