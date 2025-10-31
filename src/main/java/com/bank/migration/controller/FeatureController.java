package com.bank.migration.controller;

import com.bank.migration.model.dto.FeatureCheckRequest;
import com.bank.migration.model.dto.FeatureCheckResponse;
import com.bank.migration.service.FeatureDecisionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/features")
@RequiredArgsConstructor
@Slf4j
public class FeatureController {
    
    private final FeatureDecisionService featureDecisionService;
    
    @PostMapping("/check")
    public ResponseEntity<FeatureCheckResponse> checkFeatures(
            @RequestHeader("customerId") String customerId,
            @Valid @RequestBody FeatureCheckRequest request) {
        
        log.info("Feature check request received for customer: {} with {} features", 
                customerId, request.getFeatures().size());
        
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("customerId header is required");
        }
        
        FeatureCheckResponse response = featureDecisionService.checkFeatures(
                customerId, 
                request.getFeatures()
        );
        
        return ResponseEntity.ok(response);
    }
}

