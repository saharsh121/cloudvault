package com.cloudvault.controller;

import com.cloudvault.model.User;
import com.cloudvault.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
public class UserController {

@Autowired
private UserService userService;

@GetMapping("/")
public String home() {
return "index";
}

@PostMapping("/login")
public String loginUser(
@RequestParam("email") String email,
@RequestParam("password") String password,
HttpSession session,
RedirectAttributes redirectAttributes) {

try {
User user = userService.loginUser(email, password);
session.setAttribute("user", user);
redirectAttributes.addFlashAttribute("message",
"Welcome back, " + user.getName() + "!");
return "redirect:/dashboard";
} catch (Exception e) {
redirectAttributes.addFlashAttribute("error", e.getMessage());
return "redirect:/";
}
}

@GetMapping("/dashboard")
public String dashboard() {
return "dashboard";
}

@GetMapping("/logout")
public String logout(HttpSession session) {
session.invalidate();
return "redirect:/";
}
}