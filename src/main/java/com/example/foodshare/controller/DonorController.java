package com.example.foodshare.controller;

import com.example.foodshare.entity.Donor;
import com.example.foodshare.service.DonorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/donors")
public class DonorController {

    private final DonorService donorService;

    public DonorController(DonorService donorService) {
        this.donorService = donorService;
    }

    @PostMapping
    public ResponseEntity<Donor> create(@Valid @RequestBody Donor donor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(donorService.create(donor));
    }

    @GetMapping
    public ResponseEntity<List<Donor>> findAll() {
        return ResponseEntity.ok(donorService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Donor> findById(@PathVariable Long id) {
        return ResponseEntity.ok(donorService.findById(id));
    }
}