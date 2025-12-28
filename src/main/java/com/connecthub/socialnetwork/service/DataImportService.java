package com.connecthub.socialnetwork.service;

import com.connecthub.socialnetwork.model.User;
import com.connecthub.socialnetwork.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class DataImportService {

    private final UserRepository userRepository;

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

    @Autowired
    public DataImportService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void importUsersFromCSV(int maxUsers) {
        try {
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("data/SocialMediaUsersDataset.csv");

            if (inputStream == null) {
                System.out.println("❌ Fichier CSV non trouvé dans resources/data/");
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line = reader.readLine(); // Skip header

            int count = 0;
            Random random = new Random();

            System.out.println("🚀 Début de l'importation des utilisateurs...");

            while ((line = reader.readLine()) != null && count < maxUsers) {
                try {
                    String[] parts = line.split(",");

                    if (parts.length >= 2) {
                        String userId = parts[0].trim();
                        String name = parts[1].trim();

                        String email = generateEmail(name, userId);

                        if (userRepository.findByEmail(email).isPresent()) {
                            continue;
                        }

                        User user = new User();
                        user.setName(name);
                        user.setEmail(email);
                        user.setPassword("Password123!");  // Sans hash pour l'instant
                        user.setBio(BIOS[random.nextInt(BIOS.length)]);
                        user.setPhotoUrl("https://ui-avatars.com/api/?name=" +
                                name.replace(" ", "+") + "&size=200");
                        user.setCreatedAt(LocalDateTime.now());

                        userRepository.save(user);
                        count++;

                        if (count % 50 == 0) {
                            System.out.println("✅ " + count + " utilisateurs importés...");
                        }
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            reader.close();
            System.out.println("🎉 Importation terminée ! Total : " + count + " utilisateurs.");

        } catch (Exception e) {
            System.out.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String generateEmail(String name, String userId) {
        String cleanName = name.toLowerCase()
                .replace(" ", ".")
                .replaceAll("[^a-z.]", "");

        String shortId = userId.length() >= 3 ? userId.substring(0, 3) : userId;
        return cleanName + shortId + "@connecthub.com";
    }
}