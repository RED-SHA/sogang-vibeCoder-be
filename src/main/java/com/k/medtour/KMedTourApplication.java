package com.k.medtour;

import com.k.medtour.global.auth.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class KMedTourApplication {

    public static void main(String[] args) {
        SpringApplication.run(KMedTourApplication.class, args);
    }
}
