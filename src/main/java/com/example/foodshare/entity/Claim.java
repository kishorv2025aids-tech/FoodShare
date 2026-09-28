package com.example.foodshare.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

import java.time.LocalDateTime;

@Entity
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "food_listing_id", nullable = false, unique = true)
    @JsonIgnoreProperties("claim")
    private FoodListing foodListing;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ngo_id", nullable = false)
    @JsonIgnoreProperties("claims")
    private NGO ngo;

    private LocalDateTime claimedAt;

    @Enumerated(EnumType.STRING)
    private ClaimStatus status;

    public Claim() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FoodListing getFoodListing() {
        return foodListing;
    }

    public void setFoodListing(FoodListing foodListing) {
        this.foodListing = foodListing;
    }

    public NGO getNgo() {
        return ngo;
    }

    public void setNgo(NGO ngo) {
        this.ngo = ngo;
    }

    public LocalDateTime getClaimedAt() {
        return claimedAt;
    }

    public void setClaimedAt(LocalDateTime claimedAt) {
        this.claimedAt = claimedAt;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }
}