package com.agrimatch;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.agrimatch.**.mapper")
public class AgriMatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgriMatchApplication.class, args);
    }
}


