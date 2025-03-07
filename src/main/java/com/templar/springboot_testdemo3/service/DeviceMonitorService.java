package com.templar.springboot_testdemo3.service;

import com.jcraft.jsch.JSchException;
import com.templar.springboot_testdemo3.entity.Device;
import com.templar.springboot_testdemo3.repository.DeviceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class DeviceMonitorService {
    @Autowired
    private DeviceService deviceService;

    @Autowired
    private DeviceRepository deviceRepository;

    public void monitorAllDevices(){
        log.info("Begin monitor all devices");
        List<Device> devices = deviceRepository.findAll();

        for(Device device:devices){
            try{
                if(device.getStatus() ==Device.DeviceStatus.OFFLINE){
                    continue;
                }

                collectDeviceMetrics(device);

                device.setLastHeartbeat(LocalDateTime.now());
                device.setStatus(Device.DeviceStatus.AVAILABLE);
                deviceRepository.save(device);
            }catch (Exception e) {
                log.error("Monitor Device {} failed: {}", device.getName(), e.getMessage());
                // 更新设备状态为离线
                device.setStatus(Device.DeviceStatus.OFFLINE);
                deviceRepository.save(device);
            }
        }
        log.info("Finished monitoring all devices");
    }

    public void collectDeviceMetrics(Device device) throws JSchException {
        log.info("Collecting metrics for device: {}", device.getName());

        try{
            String cpuMetrics = deviceService.getCpuMetrics(device);
            String memoryUsage = deviceService.getMemoryMetrics(device);
            String gpuMetrics = deviceService.getGpuMetrics(device);
            String systemLoad = deviceService.getSystemLoadMetrics(device);
        } catch (JSchException e) {
            log.error("Collecting Device {} metrics failed: {}", device.getName(), e.getMessage());
            throw e;
        }
    }
}
