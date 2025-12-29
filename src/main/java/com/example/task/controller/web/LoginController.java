package com.example.task.controller.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Slf4j
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        log.info("Wyświetlenie strony logowania");
        return "login"; // -> templates/login.html
    }
}
