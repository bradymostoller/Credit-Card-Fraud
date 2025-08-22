package com.example.finance.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth/demo")
public class HelloController {

    @GetMapping
    public String hello() {
        return "Hello I am secure.";
    }
}
