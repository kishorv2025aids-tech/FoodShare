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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private FoodListingRepository foodListingRepository;

    @Mock
    private NGORepository ngoRepository;

    @Mock
    private ClaimRepository claimRepository;

    private ClaimService service;

    @BeforeEach
    void setUp() {
        service = new ClaimService(foodListingRepository, ngoRepository, claimRepository);
    }

    @Test
    void rejectsWhenListingDoesNotExist() {
        when(foodListingRepository.findByIdForUpdate(3L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.claim(3L, 4L));
    }

    @Test
    void rejectsWhenNgoDoesNotExist() {
        when(foodListingRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(availableListing()));
        when(ngoRepository.findById(4L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.claim(3L, 4L));
    }

    @Test
    void rejectsExpiredListingAndMarksItExpired() {
        FoodListing listing = availableListing();
        listing.setSafeToEatUntil(LocalDateTime.now().minusMinutes(1));
        when(foodListingRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(listing));
        when(ngoRepository.findById(4L)).thenReturn(Optional.of(new NGO()));

        assertThrows(BusinessRuleException.class, () -> service.claim(3L, 4L));

        assertEquals(FoodStatus.EXPIRED, listing.getStatus());
        verify(foodListingRepository).save(listing);
    }

    @Test
    void rejectsFoodThatIsNotAvailable() {
        FoodListing listing = availableListing();
        listing.setStatus(FoodStatus.CLAIMED);
        when(foodListingRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(listing));
        when(ngoRepository.findById(4L)).thenReturn(Optional.of(new NGO()));

        assertThrows(BusinessRuleException.class, () -> service.claim(3L, 4L));
    }

    @Test
    void rejectsDuplicateActiveClaim() {
        FoodListing listing = availableListing();
        listing.setStatus(FoodStatus.CLAIMED);
        when(foodListingRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(listing));
        when(ngoRepository.findById(4L)).thenReturn(Optional.of(new NGO()));
        when(claimRepository.existsByFoodListingIdAndStatus(3L, ClaimStatus.ACTIVE)).thenReturn(true);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> service.claim(3L, 4L));
        assertEquals("Food listing is already claimed", exception.getMessage());
    }

    @Test
    void claimSavesActiveClaimAndChangesListingToClaimed() {
        FoodListing listing = availableListing();
        NGO ngo = new NGO();
        when(foodListingRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(listing));
        when(ngoRepository.findById(4L)).thenReturn(Optional.of(ngo));
        when(claimRepository.existsByFoodListingIdAndStatus(3L, ClaimStatus.ACTIVE)).thenReturn(false);
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Claim claim = service.claim(3L, 4L);

        assertEquals(ClaimStatus.ACTIVE, claim.getStatus());
        assertEquals(listing, claim.getFoodListing());
        assertEquals(ngo, claim.getNgo());
        assertEquals(FoodStatus.CLAIMED, listing.getStatus());
        verify(foodListingRepository).save(listing);
    }

    private FoodListing availableListing() {
        FoodListing listing = new FoodListing();
        listing.setId(3L);
        listing.setSafeToEatUntil(LocalDateTime.now().plusHours(1));
        listing.setStatus(FoodStatus.AVAILABLE);
        return listing;
    }
}