package com.example.foodshare.service;

import com.example.foodshare.entity.Donor;
import com.example.foodshare.entity.FoodListing;
import com.example.foodshare.entity.FoodStatus;
import com.example.foodshare.exception.BusinessRuleException;
import com.example.foodshare.exception.ResourceNotFoundException;
import com.example.foodshare.repository.DonorRepository;
import com.example.foodshare.repository.FoodListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FoodListingServiceTest {

    @Mock
    private FoodListingRepository foodListingRepository;

    @Mock
    private DonorRepository donorRepository;

    private FoodListingService service;

    @BeforeEach
    void setUp() {
        service = new FoodListingService(foodListingRepository, donorRepository);
    }

    @Test
    void rejectsListingWhenDonorDoesNotExist() {
        when(donorRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.create(validListing(), 7L));
    }

    @Test
    void rejectsListingWithMissingSafeUntil() {
        FoodListing listing = validListing();
        listing.setSafeToEatUntil(null);
        when(donorRepository.findById(7L)).thenReturn(Optional.of(new Donor()));

        assertThrows(BusinessRuleException.class, () -> service.create(listing, 7L));
    }

    @Test
    void rejectsListingWithSafeUntilInThePast() {
        FoodListing listing = validListing();
        listing.setSafeToEatUntil(LocalDateTime.now().minusMinutes(1));
        when(donorRepository.findById(7L)).thenReturn(Optional.of(new Donor()));

        assertThrows(BusinessRuleException.class, () -> service.create(listing, 7L));
    }

    @Test
    void createSetsDonorTimestampAndAvailableStatus() {
        Donor donor = new Donor();
        when(donorRepository.findById(7L)).thenReturn(Optional.of(donor));
        when(foodListingRepository.save(any(FoodListing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FoodListing created = service.create(validListing(), 7L);

        assertEquals(donor, created.getDonor());
        assertEquals(FoodStatus.AVAILABLE, created.getStatus());
        org.junit.jupiter.api.Assertions.assertNotNull(created.getCreatedAt());
    }

    @Test
    void availableLookupMarksExpiredRowsAndDoesNotReturnThem() {
        FoodListing expired = validListing();
        expired.setSafeToEatUntil(LocalDateTime.now().minusMinutes(1));
        expired.setStatus(FoodStatus.AVAILABLE);
        FoodListing fresh = validListing();
        fresh.setSafeToEatUntil(LocalDateTime.now().plusHours(2));
        fresh.setStatus(FoodStatus.AVAILABLE);
        when(foodListingRepository.findByStatus(FoodStatus.AVAILABLE)).thenReturn(List.of(expired, fresh));

        List<FoodListing> result = service.findAvailable();

        assertEquals(List.of(fresh), result);
        assertEquals(FoodStatus.EXPIRED, expired.getStatus());
        verify(foodListingRepository).saveAll(List.of(expired));
    }

    @Test
    void onlyClaimedListingCanBeMarkedCollected() {
        FoodListing listing = validListing();
        listing.setStatus(FoodStatus.AVAILABLE);
        when(foodListingRepository.findById(9L)).thenReturn(Optional.of(listing));

        assertThrows(BusinessRuleException.class, () -> service.markCollected(9L));
    }

    @Test
    void claimedListingCanBeMarkedCollected() {
        FoodListing listing = validListing();
        listing.setStatus(FoodStatus.CLAIMED);
        when(foodListingRepository.findById(9L)).thenReturn(Optional.of(listing));
        when(foodListingRepository.save(listing)).thenReturn(listing);

        assertEquals(FoodStatus.COLLECTED, service.markCollected(9L).getStatus());
    }

    @Test
    void monthlyDivertedTotalReturnsZeroWhenThereAreNoRows() {
        when(foodListingRepository.sumQuantityByStatusAndCreatedAtRange(
                eq(FoodStatus.COLLECTED), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(0L);

        assertEquals(0, service.getMonthlyDivertedTotal(2026, 9));
    }

    @Test
    void monthlyDivertedTotalRejectsInvalidMonth() {
        assertThrows(BusinessRuleException.class, () -> service.getMonthlyDivertedTotal(2026, 13));
    }

    private FoodListing validListing() {
        FoodListing listing = new FoodListing();
        listing.setFoodType("Vegetable Rice");
        listing.setQuantity(25);
        listing.setSafeToEatUntil(LocalDateTime.now().plusHours(2));
        return listing;
    }
}