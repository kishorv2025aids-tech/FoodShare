package com.example.foodshare.service;

import com.example.foodshare.entity.Donor;
import com.example.foodshare.entity.FoodListing;
import com.example.foodshare.entity.FoodStatus;
import com.example.foodshare.exception.BusinessRuleException;
import com.example.foodshare.exception.ResourceNotFoundException;
import com.example.foodshare.repository.DonorRepository;
import com.example.foodshare.repository.FoodListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class FoodListingService {

    private final FoodListingRepository foodListingRepository;
    private final DonorRepository donorRepository;

    public FoodListingService(FoodListingRepository foodListingRepository, DonorRepository donorRepository) {
        this.foodListingRepository = foodListingRepository;
        this.donorRepository = donorRepository;
    }

    public FoodListing create(FoodListing foodListing, Long donorId) {
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found with id " + donorId));
        if (foodListing.getSafeToEatUntil() == null) {
            throw new BusinessRuleException("safeToEatUntil is required");
        }
        if (!foodListing.getSafeToEatUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("safeToEatUntil must be in the future");
        }

        foodListing.setId(null);
        foodListing.setDonor(donor);
        foodListing.setCreatedAt(LocalDateTime.now());
        foodListing.setStatus(FoodStatus.AVAILABLE);
        foodListing.setClaim(null);
        return foodListingRepository.save(foodListing);
    }

    public List<FoodListing> findAll() {
        return foodListingRepository.findAll();
    }

    @Transactional
    public List<FoodListing> findAvailable() {
        LocalDateTime now = LocalDateTime.now();
        List<FoodListing> available = foodListingRepository.findByStatus(FoodStatus.AVAILABLE);
        List<FoodListing> expired = available.stream()
                .filter(listing -> !listing.getSafeToEatUntil().isAfter(now))
                .toList();
        expired.forEach(listing -> listing.setStatus(FoodStatus.EXPIRED));
        if (!expired.isEmpty()) {
            foodListingRepository.saveAll(expired);
        }
        return available.stream()
                .filter(listing -> listing.getStatus() == FoodStatus.AVAILABLE)
                .toList();
    }

    @Transactional
    public FoodListing markCollected(Long id) {
        FoodListing listing = findListing(id);
        if (listing.getStatus() != FoodStatus.CLAIMED) {
            throw new BusinessRuleException("Only CLAIMED food can be marked as COLLECTED");
        }
        listing.setStatus(FoodStatus.COLLECTED);
        return foodListingRepository.save(listing);
    }

    @Transactional(readOnly = true)
    public long getMonthlyDivertedTotal(int year, int month) {
        YearMonth selectedMonth;
        try {
            selectedMonth = YearMonth.of(year, month);
        } catch (DateTimeException exception) {
            throw new BusinessRuleException("Year and month must be valid");
        }
        LocalDateTime start = selectedMonth.atDay(1).atStartOfDay();
        LocalDateTime end = selectedMonth.plusMonths(1).atDay(1).atStartOfDay();
        Long total = foodListingRepository.sumQuantityByStatusAndCreatedAtRange(FoodStatus.COLLECTED, start, end);
        return total == null ? 0 : total;
    }

    public FoodListing findListing(Long id) {
        return foodListingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food listing not found with id " + id));
    }
}