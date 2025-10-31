package com.bank.migration.model.dto;

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
public class FeatureCheckResponse {
    
    @JsonProperty("customerId")
    private String customerId;
    
    @JsonProperty("features")
    private List<FeatureStatus> features;
}

