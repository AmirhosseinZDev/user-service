package com.ftgo.user;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.ftgo.user.persistence.entity")
@EnableJpaRepositories(basePackages = "com.ftgo.user.persistence.repository")
public class UserServiceApplication {

    public static void main(String[] args) {
        if (new FileSystemResource("config/logback.xml").getFile().exists()) {
            System.setProperty("logging.config", "file:config/logback.xml");
        } else {
            System.setProperty("logging.config", "classpath:config/logback.xml");
        }
        new SpringApplicationBuilder(UserServiceApplication.class)
                .run(args);
    }
}
