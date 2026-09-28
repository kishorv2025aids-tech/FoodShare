package com.example.foodshare.controller;

import com.example.foodshare.entity.FoodListing;
import com.example.foodshare.service.FoodListingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/food-listings")
public class FoodListingController {

    private final FoodListingService foodListingService;

    public FoodListingController(FoodListingService foodListingService) {
        this.foodListingService = foodListingService;
    }

    @PostMapping
    public ResponseEntity<FoodListing> create(
            @RequestParam Long donorId,
            @Valid @RequestBody FoodListing foodListing) {
        return ResponseEntity.status(HttpStatus.CREATED).body(foodListingService.create(foodListing, donorId));
    }

    @GetMapping
    public ResponseEntity<List<FoodListing>> findAll() {
        return ResponseEntity.ok(foodListingService.findAll());
    }

    @GetMapping("/available")
    public ResponseEntity<List<FoodListing>> findAvailable() {
        return ResponseEntity.ok(foodListingService.findAvailable());
    }

    @PutMapping("/{id}/collected")
    public ResponseEntity<FoodListing> markCollected(@PathVariable Long id) {
        return ResponseEntity.ok(foodListingService.markCollected(id));
    }

    @GetMapping("/diverted")
    public ResponseEntity<Long> getMonthlyDivertedTotal(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(foodListingService.getMonthlyDivertedTotal(year, month));
    }
}