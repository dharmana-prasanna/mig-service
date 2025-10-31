package com.bank.migration.model.migration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MigrationApiResponse {
    
    @JsonProperty("customerId")
    private String customerId;
    
    @JsonProperty("accounts")
    private List<AccountInfo> accounts;
}

