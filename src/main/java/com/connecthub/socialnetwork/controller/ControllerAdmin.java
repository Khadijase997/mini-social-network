package com.connecthub.socialnetwork.controller;

import com.connecthub.socialnetwork.service.DataImportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class ControllerAdmin {

    private final DataImportService dataImportService;

    public ControllerAdmin(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    @GetMapping("/import-users")
    public String importUsers() {
        dataImportService.importUsersFromCSV(20);
        return "✅ Import utilisateurs terminé";
    }
}