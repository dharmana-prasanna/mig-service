package com.bank.migration.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureCheckRequest {
    
    @NotEmpty(message = "Features list cannot be empty")
    @JsonProperty("features")
    private List<String> features;
}

