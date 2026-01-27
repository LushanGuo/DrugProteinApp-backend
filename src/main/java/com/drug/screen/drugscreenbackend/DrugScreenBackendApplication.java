package com.drug.screen.drugscreenbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DrugScreenBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DrugScreenBackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            System.out.println("\n===========================================");
            System.out.println("应用启动成功！");
            System.out.println("访问地址: http://localhost:8080/");
            System.out.println("===========================================\n");
        };
    }

}
