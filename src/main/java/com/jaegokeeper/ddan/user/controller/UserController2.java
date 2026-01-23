package com.jaegokeeper.ddan.user.controller;

import com.jaegokeeper.ddan.user.service.UserService2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserController2 {

    private final UserService2 userService;
    public UserController2(UserService2 userService) {
        this.userService = userService;
    }

    @RequestMapping("/login")
    public String login() {
        return "user/login";
    }

    @RequestMapping("/signup")
    public String signup() {
        return "user/signup";
    }

    @RequestMapping("/logout")
    public String logout() {
        return "redirect:/";
    }

    @RequestMapping("/home")
    public String home() {
        return "home";
    }
}
