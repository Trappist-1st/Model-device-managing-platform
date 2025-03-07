package com.templar.springboot_testdemo3.service;

import com.templar.springboot_testdemo3.entity.VerificationCode;
import com.templar.springboot_testdemo3.repository.VerificationCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
public class VerificationCodeService {

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    public String generateCode(String username) {
        String code = String.format("%06d", new Random().nextInt(999999));
        
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setId(UUID.randomUUID().toString());
        verificationCode.setUsername(username);
        verificationCode.setCode(code);
        verificationCode.setExpireTime(LocalDateTime.now().plusMinutes(10));
        verificationCode.setUsed(false);
        
        verificationCodeRepository.save(verificationCode);
        return code;
    }

    public boolean verifyCode(String username, String code) {
        VerificationCode verificationCode = verificationCodeRepository
            .findByUsernameAndCodeAndUsedFalse(username, code)
            .orElse(null);
            
        if (verificationCode == null || 
            verificationCode.getExpireTime().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        verificationCode.setUsed(true);
        verificationCodeRepository.save(verificationCode);
        return true;
    }
} 