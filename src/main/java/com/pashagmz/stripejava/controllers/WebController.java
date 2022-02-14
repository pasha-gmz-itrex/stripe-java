package com.pashagmz.stripejava.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index(Model model ) {
        return "index";
    }

    @GetMapping("/save-card")
    public String saveCard(Model model ) {
        return "save-card";
    }

    @GetMapping("/card-status")
    public String cardStatus(Model model ) {
        return "card-status";
    }

    @GetMapping("/stripe")
    public String stripe(Model model ) {
        return "stripe";
    }

    @GetMapping("/payment-method")
    public String paymentMethod(Model model ) {
        return "payment-method";
    }

    @GetMapping("/payment-method-status")
    public String paymentMethodStatus(Model model ) {
        return "payment-method-status";
    }
}
