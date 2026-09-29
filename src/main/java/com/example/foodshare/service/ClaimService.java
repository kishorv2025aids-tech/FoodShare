package com.example.foodshare.service;

import com.example.foodshare.entity.Claim;
import com.example.foodshare.entity.ClaimStatus;
import com.example.foodshare.entity.FoodListing;
import com.example.foodshare.entity.FoodStatus;
import com.example.foodshare.entity.NGO;
import com.example.foodshare.exception.BusinessRuleException;
import com.example.foodshare.exception.ResourceNotFoundException;
import com.example.foodshare.repository.ClaimRepository;
import com.example.foodshare.repository.FoodListingRepository;
import com.example.foodshare.repository.NGORepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClaimService {

    private final FoodListingRepository foodListingRepository;
    private final NGORepository ngoRepository;
    private final ClaimRepository claimRepository;

    public ClaimService(
            FoodListingRepository foodListingRepository,
            NGORepository ngoRepository,
            ClaimRepository claimRepository) {
        this.foodListingRepository = foodListingRepository;
        this.ngoRepository = ngoRepository;
        this.claimRepository = claimRepository;
    }

    @Transactional(readOnly = true)
    public List<Claim> findAll() {
        return claimRepository.findAll();
    }

    @Transactional(noRollbackFor = BusinessRuleException.class)
    public Claim claim(Long listingId, Long ngoId) {
        FoodListing listing = foodListingRepository.findByIdForUpdate(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Food listing not found with id " + listingId));
        NGO ngo = ngoRepository.findById(ngoId)
                .orElseThrow(() -> new ResourceNotFoundException("NGO not found with id " + ngoId));

        if (!listing.getSafeToEatUntil().isAfter(LocalDateTime.now())) {
            if (listing.getStatus() == FoodStatus.AVAILABLE) {
                listing.setStatus(FoodStatus.EXPIRED);
                foodListingRepository.save(listing);
            }
            throw new BusinessRuleException("Expired food cannot be claimed");
        }
        if (claimRepository.existsByFoodListingIdAndStatus(listingId, ClaimStatus.ACTIVE)) {
            throw new BusinessRuleException("Food listing is already claimed");
        }
        if (listing.getStatus() != FoodStatus.AVAILABLE) {
            throw new BusinessRuleException("Only AVAILABLE food can be claimed");
        }

        Claim claim = new Claim();
        claim.setFoodListing(listing);
        claim.setNgo(ngo);
        claim.setClaimedAt(LocalDateTime.now());
        claim.setStatus(ClaimStatus.ACTIVE);
        listing.setStatus(FoodStatus.CLAIMED);

        Claim savedClaim = claimRepository.save(claim);
        foodListingRepository.save(listing);
        return savedClaim;
    }
}