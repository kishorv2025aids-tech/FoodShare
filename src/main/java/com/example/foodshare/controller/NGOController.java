package com.example.foodshare.controller;

import com.example.foodshare.entity.NGO;
import com.example.foodshare.service.NGOService;
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
@RequestMapping("/api/ngos")
public class NGOController {

    private final NGOService ngoService;

    public NGOController(NGOService ngoService) {
        this.ngoService = ngoService;
    }

    @PostMapping
    public ResponseEntity<NGO> create(@Valid @RequestBody NGO ngo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ngoService.create(ngo));
    }

    @GetMapping
    public ResponseEntity<List<NGO>> findAll() {
        return ResponseEntity.ok(ngoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NGO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ngoService.findById(id));
    }
}