package com.example.complaints;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@OpenAPIDefinition(
        info = @Info(
                title = "Complaints API",
                version = "1.0",
                description = "RESTful API for managing complaints. It supports creation, update, and retrieval of complaints. " +
                        "Duplicates are handled by incrementing the counter. The country of the complaint is determined " +
                        "from the client's IP address. In case of external API failure, a configurable fallback is used."
        )
)
public class ComplaintsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ComplaintsApplication.class, args);
    }
}
