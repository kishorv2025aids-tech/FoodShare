package com.example.foodshare.repository;

import com.example.foodshare.entity.Claim;
import com.example.foodshare.entity.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    boolean existsByFoodListingIdAndStatus(Long foodListingId, ClaimStatus status);
}