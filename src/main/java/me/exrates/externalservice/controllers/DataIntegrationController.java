package me.exrates.externalservice.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DataIntegrationController {

    @GetMapping("/test")
    public void test() {
        System.out.println("test");
    }

}
