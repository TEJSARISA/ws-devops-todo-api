package com.ws.todo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class WsDevopsTodoApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WsDevopsTodoApiApplication.class, args);
    }
}
