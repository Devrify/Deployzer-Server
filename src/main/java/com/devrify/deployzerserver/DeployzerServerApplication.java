package com.devrify.deployzerserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableScheduling
@EnableSwagger2
@SpringBootApplication
public class DeployzerServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeployzerServerApplication.class, args);
    }

}
