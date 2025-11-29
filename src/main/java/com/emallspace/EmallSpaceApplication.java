package com.emallspace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EmallSpaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmallSpaceApplication.class, args);
    }

}
