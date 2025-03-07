package com.templar.springboot_testdemo3.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class VerificationCode {
    @Id
    private String id;
    private String username;
    private String code;
    private LocalDateTime expireTime;
    private boolean used;
} 