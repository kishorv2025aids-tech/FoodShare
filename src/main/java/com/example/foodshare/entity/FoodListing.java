package com.example.foodshare.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

@Entity
public class FoodListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "donor_id", nullable = false)
    @JsonIgnoreProperties("foodListings")
    private Donor donor;

    @NotBlank(message = "Food type is required")
    private String foodType;

    @Positive(message = "Quantity must be greater than zero")
    private int quantity;

    @NotNull(message = "safeToEatUntil is required")
    private LocalDateTime safeToEatUntil;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private FoodStatus status;

    @OneToOne(mappedBy = "foodListing")
    @JsonIgnore
    private Claim claim;

    public FoodListing() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Donor getDonor() {
        return donor;
    }

    public void setDonor(Donor donor) {
        this.donor = donor;
    }

    public String getFoodType() {
        return foodType;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getSafeToEatUntil() {
        return safeToEatUntil;
    }

    public void setSafeToEatUntil(LocalDateTime safeToEatUntil) {
        this.safeToEatUntil = safeToEatUntil;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public FoodStatus getStatus() {
        return status;
    }

    public void setStatus(FoodStatus status) {
        this.status = status;
    }

    public Claim getClaim() {
        return claim;
    }

    public void setClaim(Claim claim) {
        this.claim = claim;
    }
}