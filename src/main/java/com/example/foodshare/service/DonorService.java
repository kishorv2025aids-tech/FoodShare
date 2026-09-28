package com.example.foodshare.service;

import com.example.foodshare.entity.Donor;
import com.example.foodshare.exception.ResourceNotFoundException;
import com.example.foodshare.repository.DonorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DonorService {

    private final DonorRepository donorRepository;

    public DonorService(DonorRepository donorRepository) {
        this.donorRepository = donorRepository;
    }

    public Donor create(Donor donor) {
        donor.setId(null);
        return donorRepository.save(donor);
    }

    public List<Donor> findAll() {
        return donorRepository.findAll();
    }

    public Donor findById(Long id) {
        return donorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found with id " + id));
    }
}