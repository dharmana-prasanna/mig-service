package com.bank.migration.config;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class DroolsConfig {
    
    // Excel decision table - business users can easily modify this file
    private static final String RULES_EXCEL_PATH = "rules/migration-rules.xlsx";
    
    // Fallback to CSV if XLSX not available (CSV can be opened in Excel)
    private static final String RULES_CSV_PATH = "rules/migration-rules.csv";
    
    @Bean
    public KieContainer kieContainer() {
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        
        // Try to load Excel file first, fallback to CSV
        try {
            log.info("Loading decision table from: {}", RULES_EXCEL_PATH);
            kieFileSystem.write(ResourceFactory.newClassPathResource(RULES_EXCEL_PATH));
        } catch (Exception e) {
            log.warn("Excel file not found, trying CSV: {}", RULES_CSV_PATH);
            kieFileSystem.write(ResourceFactory.newClassPathResource(RULES_CSV_PATH));
        }
        
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        
        // Log any errors or warnings during rule compilation
        if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
            log.error("Errors building rules: {}", kieBuilder.getResults().toString());
            throw new RuntimeException("Error building Drools rules: " + kieBuilder.getResults().toString());
        }
        
        if (kieBuilder.getResults().hasMessages(Message.Level.WARNING)) {
            log.warn("Warnings building rules: {}", kieBuilder.getResults().toString());
        }
        
        KieModule kieModule = kieBuilder.getKieModule();
        log.info("Successfully loaded migration rules decision table");
        return kieServices.newKieContainer(kieModule.getReleaseId());
    }
}

