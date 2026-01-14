package com.connecthub.socialnetwork;

import com.connecthub.socialnetwork.repository.UserRepository;
import com.connecthub.socialnetwork.service.DataImportService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

@SpringBootApplication
public class SocialNetworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialNetworkApplication.class, args);
    }

    /**
     * Import CSV au démarrage (optionnel et configurable).
     * Objectif: rendre visibles les utilisateurs du dataset sur le site (recherche/suggestions).
     */
    @Bean
    CommandLineRunner importCsvUsersOnStartup(
            DataImportService dataImportService,
            UserRepository userRepository,
            @Value("${connecthub.csv-import.enabled:false}") boolean enabled,
            @Value("${connecthub.csv-import.max-users:200}") int maxUsers,
            @Value("${connecthub.csv-import.only-if-empty:true}") boolean onlyIfEmpty
    ) {
        return args -> {
            if (!enabled) {
                return;
            }
            if (onlyIfEmpty && userRepository.count() > 0) {
                return;
            }
            dataImportService.importUsersFromCSV(maxUsers);
        };
    }
}
