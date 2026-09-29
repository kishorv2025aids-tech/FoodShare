package com.example.foodshare.repository;

import com.example.foodshare.entity.FoodShareAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FoodShareAccountRepository extends JpaRepository<FoodShareAccount, Long> {

    Optional<FoodShareAccount> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}