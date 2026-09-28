package com.example.foodshare.service;

import com.example.foodshare.entity.NGO;
import com.example.foodshare.exception.ResourceNotFoundException;
import com.example.foodshare.repository.NGORepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NGOService {

    private final NGORepository ngoRepository;

    public NGOService(NGORepository ngoRepository) {
        this.ngoRepository = ngoRepository;
    }

    public NGO create(NGO ngo) {
        ngo.setId(null);
        return ngoRepository.save(ngo);
    }

    public List<NGO> findAll() {
        return ngoRepository.findAll();
    }

    public NGO findById(Long id) {
        return ngoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NGO not found with id " + id));
    }
}