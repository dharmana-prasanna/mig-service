package com.bank.migration.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureStatus {
    
    @JsonProperty("feature")
    private String feature;
    
    @JsonProperty("enabled")
    private boolean enabled;
    
    @JsonProperty("reason")
    private String reason;
}

