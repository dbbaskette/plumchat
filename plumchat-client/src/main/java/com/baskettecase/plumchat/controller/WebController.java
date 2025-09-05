package com.baskettecase.plumchat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebController {

    @RequestMapping(value = {"/"})
    public String index() {
        return "forward:/index.html";
    }

    // Forward all non-API routes to React frontend
    @RequestMapping(value = {"/app/**", "/chat/**", "/settings/**"})
    public String frontend() {
        return "forward:/index.html";
    }
}
