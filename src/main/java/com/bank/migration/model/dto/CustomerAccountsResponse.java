package com.bank.migration.model.dto;

import com.bank.migration.model.migration.AccountInfo;
import com.bank.migration.model.rules.CustomerStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerAccountsResponse {
    
    @JsonProperty("customerId")
    private String customerId;
    
    @JsonProperty("customerStatus")
    private CustomerStatus customerStatus;
    
    @JsonProperty("accounts")
    private List<AccountInfo> accounts;
    
    @JsonProperty("featureSuppressionInfo")
    private Map<String, FeatureStatus> featureSuppressionInfo;
}

