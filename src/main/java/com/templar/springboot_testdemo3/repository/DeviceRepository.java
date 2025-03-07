package com.templar.springboot_testdemo3.repository;

import com.templar.springboot_testdemo3.entity.Device;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device,Integer> {
    // 自定义查询：根据状态查找设备
    List<Device> findByStatus(Device.DeviceStatus status);

    List<Device> findAllByStatusEquals(Device.DeviceStatus status);

    //自定义更新，修改设备状态
    @Transactional
    @Modifying
    @Query("UPDATE Device d SET d.status = :status WHERE d.id = :id")
    int updateDeviceStatus(Integer id, Device.DeviceStatus status);

    //根据ip地址查找设备
    Device findByIpAddress(String ipAddress);

    Optional<Device> findByName(String name);

    @Query("SELECT d FROM Device d WHERE d.status= 'AVAILABLE' AND d.gpuCount>=:minGpuCount")
    List<Device> findAvailableDevicesWithMinGpu(int minGpuCount);

    @Query("SELECT d FROM Device d WHERE d.status = 'AVAILABLE' ORDER BY d.currentLoad ASC")
    List<Device> findAvailableDevicesSortedByLoad();

    @Query("SELECT d FROM Device d WHERE d.status <> 'OFFLINE' AND d.lastConnected <= :cutoffTime")
    List<Device> findPotentiallyOfflineDevices(java.time.LocalDateTime cutoffTime);
}
