package com.connecthub.socialnetwork.controller;

import com.connecthub.socialnetwork.service.DataImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataController {

    private final DataImportService dataImportService;

    @Autowired
    public DataController(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    @GetMapping("/api/import")
    public String importUsers(@RequestParam(defaultValue = "100") int max) {
        System.out.println("🔥 Lancement de l'import de " + max + " utilisateurs...");
        dataImportService.importUsersFromCSV(max);
        return "✅ Import lancé ! Vérifiez la console IntelliJ. Total demandé : " + max + " utilisateurs.";
    }

    @GetMapping("/api/test")
    public String test() {
        return "✅ API fonctionne correctement !";
    }
}