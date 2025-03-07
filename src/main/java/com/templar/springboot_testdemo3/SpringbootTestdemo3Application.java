package com.templar.springboot_testdemo3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringbootTestdemo3Application {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootTestdemo3Application.class, args);
    }

}
