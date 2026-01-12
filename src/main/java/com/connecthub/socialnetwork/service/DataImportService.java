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
            Random random = new Random();

            System.out.println("🚀 Import utilisateurs en cours...");

            String line;
            while ((line = reader.readLine()) != null && count < maxUsers) {

                String[] parts = line.split(",");

                if (parts.length < 2) continue;

                String userId = parts[0].trim();
                String name = parts[1].trim();

                String email = generateEmail(name, userId);

                if (userRepository.findByEmail(email).isPresent()) {
                    continue;
                }

                User user = new User();
                user.setName(name);
                user.setEmail(email);
                user.setPassword("Password123!");
                user.setBio(BIOS[random.nextInt(BIOS.length)]);
                user.setProfileImage(
                        "https://ui-avatars.com/api/?name=" +
                                name.replace(" ", "+") + "&size=200"
                );
                user.setCreatedAt(LocalDateTime.now());

                userRepository.save(user);
                count++;
            }

            reader.close();
            System.out.println("🎉 Import terminé : " + count + " utilisateurs");

        } catch (Exception e) {
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