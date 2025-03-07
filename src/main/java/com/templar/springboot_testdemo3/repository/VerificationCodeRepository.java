package com.templar.springboot_testdemo3.repository;

import com.templar.springboot_testdemo3.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, String> {
    Optional<VerificationCode> findByUsernameAndCodeAndUsedFalse(String username, String code);
} 