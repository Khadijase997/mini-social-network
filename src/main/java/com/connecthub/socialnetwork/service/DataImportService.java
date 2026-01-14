package com.connecthub.socialnetwork.service;

import com.connecthub.socialnetwork.model.User;
import com.connecthub.socialnetwork.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DataImportService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataImportService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private static final String[] BIOS = {
            "Passionné de technologie 💻",
            "Voyageur dans l'âme 🌍",
            "Amateur de café ☕",
            "Étudiant en informatique 🎓",
            "Développeur passionné 🚀",
            "Fan de sport 💪",
            "Cinéphile 🎬",
            "Amoureux de la nature 🌿",
            "Photographe amateur 📸",
            "Musicien 🎸"
    };


    public void importUsersFromCSV(int maxUsers) {

        try {
            InputStream inputStream = getClass()
                    .getClassLoader()
                    .getResourceAsStream("data/SocialMediaUsersDataset.csv");

            if (inputStream == null) {
                System.out.println("❌ Fichier CSV non trouvé");
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            // skip header
            reader.readLine();

            int count = 0;
            int skipped = 0;
            Random random = new Random();
            List<User> importedUsers = new java.util.ArrayList<>();

            System.out.println("🚀 Import utilisateurs en cours...");

            String line;
            while ((line = reader.readLine()) != null && count < maxUsers) {
                try {
                    // Parser la ligne CSV en tenant compte des guillemets et virgules dans les champs
                    String[] parts = parseCSVLine(line);

                    if (parts.length < 6) {
                        skipped++;
                        continue;
                    }

                    String userId = parts[0].trim();
                    String name = parts[1].trim();
                    // Champs disponibles dans le CSV (non utilisés pour l’instant)
                    // String gender = parts.length > 2 ? parts[2].trim() : "";
                    // String dob = parts.length > 3 ? parts[3].trim() : "";
                    String interestsStr = parts.length > 4 ? parts[4].trim() : "";
                    String city = parts.length > 5 ? parts[5].trim() : "";
                    String country = parts.length > 6 ? parts[6].trim() : "";

                    String email = generateEmail(name, userId);

                    // Vérifier si l'utilisateur existe déjà
                    if (userRepository.findByEmail(email).isPresent()) {
                        skipped++;
                        continue;
                    }

                    User user = new User();
                    user.setName(name);
                    user.setEmail(email);
                    // Hasher le mot de passe avec BCrypt (mot de passe par défaut pour les utilisateurs importés)
                    user.setPassword(passwordEncoder.encode("Password123!"));
                    
                    // Bio avec informations du CSV
                    StringBuilder bioBuilder = new StringBuilder();
                    if (city != null && !city.isEmpty()) {
                        bioBuilder.append("📍 ").append(city);
                        if (country != null && !country.isEmpty()) {
                            bioBuilder.append(", ").append(country);
                        }
                    }
                    if (bioBuilder.length() > 0) {
                        bioBuilder.append(" | ");
                    }
                    bioBuilder.append(BIOS[random.nextInt(BIOS.length)]);
                    user.setBio(bioBuilder.toString());
                    
                    user.setProfileImage(
                            "https://ui-avatars.com/api/?name=" +
                                    name.replace(" ", "+") + "&size=200"
                    );
                    user.setCreatedAt(LocalDateTime.now());

                    // Parser les intérêts
                    if (interestsStr != null && !interestsStr.isEmpty()) {
                        Set<String> interests = parseInterests(interestsStr);
                        user.setInterests(interests);
                    }

                    userRepository.save(user);
                    importedUsers.add(user);
                    count++;
                    
                    if (count % 100 == 0) {
                        System.out.println("   ✓ " + count + " utilisateurs importés...");
                    }
                } catch (Exception e) {
                    skipped++;
                    System.err.println("Erreur lors de l'import de la ligne: " + e.getMessage());
                }
            }

            reader.close();

            // Créer des connexions entre les utilisateurs importés pour activer recommandations/feed
            // On ne touche qu'aux nouveaux utilisateurs pour éviter de recharger tout le graphe à chaque démarrage
            if (!importedUsers.isEmpty()) {
                connectImportedUsers(importedUsers, 5);
            }

            System.out.println("🎉 Import terminé : " + count + " utilisateurs importés, " + skipped + " ignorés");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'import: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Parse une ligne CSV en tenant compte des guillemets
     */
    private String[] parseCSVLine(String line) {
        java.util.List<String> result = new java.util.ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentField = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        result.add(currentField.toString());
        
        return result.toArray(new String[0]);
    }

    /**
     * Parse les intérêts depuis une chaîne formatée comme "'Interest1', 'Interest2'"
     */
    private Set<String> parseInterests(String interestsStr) {
        Set<String> interests = new HashSet<>();
        
        if (interestsStr == null || interestsStr.trim().isEmpty()) {
            return interests;
        }
        
        // Nettoyer la chaîne : enlever les guillemets simples et espaces
        String cleaned = interestsStr.trim();
        
        // Si la chaîne commence et se termine par des guillemets simples, les enlever
        if (cleaned.startsWith("'") && cleaned.endsWith("'")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        
        // Diviser par les virgules et nettoyer chaque intérêt
        String[] parts = cleaned.split("',\\s*'");
        for (String part : parts) {
            String interest = part.trim()
                    .replace("'", "")
                    .replace("\"", "")
                    .trim();
            if (!interest.isEmpty()) {
                interests.add(interest);
            }
        }
        
        return interests;
    }

    private String generateEmail(String name, String userId) {
        String cleanName = name.toLowerCase()
                .replace(" ", ".")
                .replaceAll("[^a-z.]", "");

        String shortId = userId.length() >= 3 ? userId.substring(0, 3) : userId;
        return cleanName + shortId + "@connecthub.com";
    }

    /**
     * Crée des connexions mutuelles entre utilisateurs importés (@connecthub.com)
     * pour alimenter les recommandations d'amis/feed.
     */
    private void connectImportedUsers(List<User> users, int maxConnectionsPerUser) {
        if (users == null || users.size() < 2) {
            return;
        }

        // Indexer par email pour éviter les duplications
        for (int i = 0; i < users.size(); i++) {
            User u1 = users.get(i);
            int currentDegree = u1.getFriends() != null ? u1.getFriends().size() : 0;
            if (currentDegree >= maxConnectionsPerUser) continue;

            // Choisir des candidats différents, prioriser intérêts communs
            for (int j = i + 1; j < users.size() && currentDegree < maxConnectionsPerUser; j++) {
                User u2 = users.get(j);
                if (u1.getId() != null && u1.getId().equals(u2.getId())) continue;
                if (u1.getFriends().contains(u2)) continue;

                boolean shareInterest = false;
                if (u1.getInterests() != null && u2.getInterests() != null) {
                    shareInterest = u1.getInterests().stream().anyMatch(u2.getInterests()::contains);
                }

                // Connecter surtout s'ils partagent au moins un intérêt
                if (shareInterest || currentDegree < 2) {
                    u1.getFriends().add(u2);
                    u2.getFriends().add(u1);
                    currentDegree++;
                }
            }
        }
        userRepository.saveAll(users);
    }
}