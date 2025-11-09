package com.gaming.leaderboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main application class for Gaming Leaderboard System
 * Built with Java 21 and Spring Boot 3.x
 */
@SpringBootApplication
@EnableCaching
public class LeaderboardApplication {

    public static void main(String[] args) {
        // Enable virtual threads for improved concurrency (Java 21 feature)
        System.setProperty("spring.threads.virtual.enabled", "true");
        SpringApplication.run(LeaderboardApplication.class, args);
    }
}
