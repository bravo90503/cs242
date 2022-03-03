package edu.ucr.cs242.web;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.ucr.cs242.repo.UserRepository;
import edu.ucr.cs242.repo.model.User;

@Controller
public class SimpleController {
	@Autowired
	private UserRepository userRepository;
	@Value("${spring.application.name}")
	private String appName;

	@GetMapping("/")
	public String homePage(Model model) {
		model.addAttribute("appName", appName);
		return "home";
	}

	@GetMapping(value = "/insert")
	public String insert(User user) {

		return "insert";
	}

	@PostMapping(value = "/adduser")
	public String addUser(User user, RedirectAttributes redir) {
		userRepository.save(user);
		redir.addFlashAttribute(user);
		return "redirect:read";
	}
	
	@GetMapping(value = "/find") 
	public String find(User user){
		return "find";
	}
	
	@PostMapping(value = "/finduser")
	public String findUser(User user, RedirectAttributes redir) {
		redir.addFlashAttribute(user);
		return "redirect:read";
	}

	@GetMapping("/read")
	public String read(Model model, User param) {
		if (param.getId() != null) {
			Optional<User> user = userRepository.findById(param.getId());
			if (user != null && user.isPresent()) {
				String userId = user.get().getId();
				model.addAttribute("userId", userId);
			}
		}
		return "read";
	}
}
