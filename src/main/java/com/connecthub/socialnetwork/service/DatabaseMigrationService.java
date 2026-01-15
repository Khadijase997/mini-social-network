package com.connecthub.socialnetwork.service;

import com.connecthub.socialnetwork.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DatabaseMigrationService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigrationService.class);
    private final Driver driver; // Utilisation du driver natif pour être sûr de contourner le mapping SDN

    public DatabaseMigrationService(Driver driver) {
        this.driver = driver;
    }

    @Override
    public void run(String... args) {
        logger.info("🚀 Réparation forcée des IDs via Driver natif...");
        
        try (Session session = driver.session()) {
            // On cherche tous les Users qui n'ont pas la propriété 'id'
            int fixedCount = session.executeWrite(tx -> {
                Result result = tx.run("MATCH (u:User) WHERE u.id IS NULL RETURN u.email as email");
                int count = 0;
                while (result.hasNext()) {
                    String email = result.next().get("email").asString();
                    String newId = UUID.randomUUID().toString();
                    tx.run("MATCH (u:User {email: $email}) SET u.id = $id", 
                        org.neo4j.driver.Values.parameters("email", email, "id", newId));
                    logger.warn("✅ ID généré pour {} : {}", email, newId);
                    count++;
                }
                return count;
            });

            if (fixedCount > 0) {
                logger.info("🎉 Réparation terminée : {} utilisateurs impactés.", fixedCount);
            } else {
                logger.info("✨ Aucun utilisateur sans ID détecté via Driver natif.");
            }
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la réparation native : {}", e.getMessage());
        }
    }
}
