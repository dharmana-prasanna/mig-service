package com.bank.migration.model.migration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountInfo {
    
    @JsonProperty("accountId")
    private String accountId;
    
    @JsonProperty("accountType")
    private AccountType accountType;
    
    @JsonProperty("migrationStatus")
    private MigrationStatus migrationStatus;
    
    @JsonProperty("migrationWave")
    private MigrationWave migrationWave;
    
    @JsonProperty("migrationDate")
    private LocalDate migrationDate;
}

