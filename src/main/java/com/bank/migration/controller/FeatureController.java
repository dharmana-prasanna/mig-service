package com.bank.migration.controller;

import com.bank.migration.model.dto.CustomerAccountsRequest;
import com.bank.migration.model.dto.CustomerAccountsResponse;
import com.bank.migration.model.dto.FeatureCheckRequest;
import com.bank.migration.model.dto.FeatureCheckResponse;
import com.bank.migration.service.FeatureDecisionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    
    @PostMapping("/customers/{customerId}/accounts")
    public ResponseEntity<CustomerAccountsResponse> getAccountsWithFeatures(
            @PathVariable("customerId") String customerId,
            @RequestParam(name = "withFeatures", defaultValue = "false") boolean withFeatures,
            @RequestBody(required = false) CustomerAccountsRequest request) {
        
        log.info("Get accounts request for customer: {} (withFeatures: {})", customerId, withFeatures);
        
        // Extract features from request body if provided
        List<String> features = null;
        if (withFeatures && request != null && request.getFeatures() != null) {
            features = request.getFeatures();
            log.debug("Requested {} features for suppression info", features.size());
        }
        
        CustomerAccountsResponse response = featureDecisionService.getAccountsWithFeatures(
                customerId,
                withFeatures,
                features
        );
        
        return ResponseEntity.ok(response);
    }
}

