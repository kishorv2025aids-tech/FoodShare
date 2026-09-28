package com.example.foodshare.controller;

import com.example.foodshare.entity.Claim;
import com.example.foodshare.service.ClaimService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping
    public ResponseEntity<Claim> claim(
            @RequestParam Long listingId,
            @RequestParam Long ngoId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(claimService.claim(listingId, ngoId));
    }
}