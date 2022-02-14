package com.pashagmz.stripejava;

import com.stripe.Stripe;
import javax.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StripeJavaApplication {

    @PostConstruct
    public void postInit() {
        Stripe.apiKey = "1234";
    }

    public static void main(String[] args) {
        SpringApplication.run(StripeJavaApplication.class, args);
    }

}
