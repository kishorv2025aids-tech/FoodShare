package com.example.foodshare.service;

import com.example.foodshare.entity.AccountRole;
import com.example.foodshare.entity.Donor;
import com.example.foodshare.entity.FoodShareAccount;
import com.example.foodshare.entity.NGO;
import com.example.foodshare.exception.BusinessRuleException;
import com.example.foodshare.repository.DonorRepository;
import com.example.foodshare.repository.FoodShareAccountRepository;
import com.example.foodshare.repository.NGORepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AccountService {

    private final FoodShareAccountRepository accountRepository;
    private final DonorRepository donorRepository;
    private final NGORepository ngoRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(
            FoodShareAccountRepository accountRepository,
            DonorRepository donorRepository,
            NGORepository ngoRepository,
            PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.donorRepository = donorRepository;
        this.ngoRepository = ngoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public FoodShareAccount signUp(String name, String email, String phone, String password, AccountRole role) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (accountRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BusinessRuleException("An account with this email already exists");
        }

        FoodShareAccount account = new FoodShareAccount();
        account.setEmail(normalizedEmail);
        account.setPasswordHash(passwordEncoder.encode(password));
        account.setRole(role);
        if (role == AccountRole.DONOR) {
            Donor donor = new Donor();
            donor.setName(name.trim());
            donor.setEmail(normalizedEmail);
            donor.setPhone(phone.trim());
            account.setDonor(donorRepository.save(donor));
        } else {
            NGO ngo = new NGO();
            ngo.setName(name.trim());
            ngo.setEmail(normalizedEmail);
            ngo.setPhone(phone.trim());
            account.setNgo(ngoRepository.save(ngo));
        }
        return accountRepository.save(account);
    }
}