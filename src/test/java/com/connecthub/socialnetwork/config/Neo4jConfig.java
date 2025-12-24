package com.connecthub.socialnetwork.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@Configuration
@EnableNeo4jRepositories(basePackages = "com.connecthub.socialnetwork.repository")
public class Neo4jConfig {
    // Configuration automatique par Spring Boot
}