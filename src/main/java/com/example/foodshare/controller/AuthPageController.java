package com.example.foodshare.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthPageController {

    @GetMapping("/signin")
    public String signInPage() {
        return "forward:/signin.html";
    }

    @GetMapping("/signup")
    public String signUpPage() {
        return "forward:/signup.html";
    }

    @GetMapping("/entities")
    public String entitiesPage() {
        return "forward:/entities.html";
    }
}