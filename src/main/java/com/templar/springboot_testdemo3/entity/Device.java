package com.templar.springboot_testdemo3.entity;

// src/main/java/com/templar/springboot_testdemo3/entity/Device.java

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "devices")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("ipAddress")
    private String ipAddress;
    @JsonProperty("port")
    private int port; // SSH端口

    @JsonProperty("username")
    private String username; // SSH用户名

    @JsonProperty("password")
    private String password; // SSH密码

    @JsonProperty("credential")
    private String credential; // 加密后的密码或密钥

    @Enumerated(EnumType.STRING)
    @JsonProperty("status")
    private DeviceStatus status; // 状态枚举
    @JsonProperty("gpu_info")
    private String gpuInfo; // GPU型号

    @JsonProperty("last_heartbeat")
    private LocalDateTime lastHeartbeat; // 最后心跳时间

    @JsonProperty("gpu_count")
    private int gpuCount;

    @JsonProperty("current_load")
    @Column(name = "current_load")
    private Double currentLoad;

    @JsonProperty("last_connected")
    private LocalDateTime lastConnected;

    @JsonProperty("ssh_key_path")
    private String sshKeyPath;
    // 枚举定义
    public enum DeviceStatus {
        AVAILABLE, OFFLINE, IDLE, TRAINING, MAINTENANCE
    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    public String getGpuInfo() {
        return gpuInfo;
    }

    public void setGpuInfo(String gpuInfo) {
        this.gpuInfo = gpuInfo;
    }

    public LocalDateTime getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(LocalDateTime lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }
}
