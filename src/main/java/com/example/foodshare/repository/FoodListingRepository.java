package com.example.foodshare.repository;

import com.example.foodshare.entity.FoodListing;
import com.example.foodshare.entity.FoodStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FoodListingRepository extends JpaRepository<FoodListing, Long> {

    List<FoodListing> findByStatus(FoodStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM FoodListing f WHERE f.id = :id")
    Optional<FoodListing> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT COALESCE(SUM(f.quantity), 0) FROM FoodListing f "
            + "WHERE f.status = :status AND f.createdAt >= :start AND f.createdAt < :end")
    Long sumQuantityByStatusAndCreatedAtRange(
            @Param("status") FoodStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}