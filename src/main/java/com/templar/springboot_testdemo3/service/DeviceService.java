package com.templar.springboot_testdemo3.service;

import com.jcraft.jsch.JSchException;
import com.templar.springboot_testdemo3.entity.Device;
import com.templar.springboot_testdemo3.exception.DeviceNotFoundException;
import com.templar.springboot_testdemo3.repository.DeviceRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class DeviceService {

    @Autowired
    private SSHService sshClientManager;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private SSHService sshService;

    public void checkDeviceHealth(Integer deviceId) throws JSchException {
        Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new DeviceNotFoundException());
        ;
        SSHService.CommandResult result = sshClientManager.executeCommand(
                device,
                "nvidia-smi --query-gpu=utilization.gpu --format=csv"
        );

        //更新设备状态和心跳时间
        if (result.isSuccess()) {
            device.setStatus(Device.DeviceStatus.IDLE);
        } else {
            device.setStatus(Device.DeviceStatus.OFFLINE);
        }
        device.setLastHeartbeat(LocalDateTime.now());
        deviceRepository.save(device);
    }

    public Device createDevice(Device device) {
        device.setLastHeartbeat(LocalDateTime.now());
        return deviceRepository.save(device);
    }

    public List<Device> getAllDevices() {
        deviceRepository.flush();
        return deviceRepository.findAll();
    }

    //Get all available devices
    public List<Device> getAvailableDevices() {
        deviceRepository.flush();
        return deviceRepository.findAllByStatusEquals(Device.DeviceStatus.AVAILABLE);
    }

    public Optional<Device> getDeviceById(Integer id) {
        return deviceRepository.findById(id);
    }

    public Optional<Device> getDeviceByName(String name) {
        return deviceRepository.findByName(name);
    }

    public List<Device> findByStatus(Device.DeviceStatus status) {
        return deviceRepository.findByStatus(status);
    }

    private String decrypt(String credential) {
        // 添加实际的解密逻辑（需要根据加密方式实现）
        return credential; // 暂时返回原文
    }

    @Transactional
    public Device registerDevice(Device device) {
        log.info("Registering device: {}", device.getName());
        return deviceRepository.save(device);
    }

    public void deleteById(Integer id) {
        deviceRepository.deleteById(id);
    }

    public Device updateDeviceStatus(Integer id, Device.DeviceStatus status) {
        deviceRepository.updateDeviceStatus(id, status);
        return deviceRepository.findById(id)
                .orElseThrow(DeviceNotFoundException::new);
    }

    public LocalDateTime getLastHeartbeat(Integer id) {
        return deviceRepository.findById(id)
                .map(Device::getLastHeartbeat)
                .orElseThrow(DeviceNotFoundException::new);
    }

    //Find the best available device for a job based on GPU requirements
    public Optional<Device> findBestDeviceForJob(int requiredGpus) {
        List<Device> eligibleDevices = deviceRepository.findAvailableDevicesWithMinGpu(requiredGpus);
        return eligibleDevices.stream()
                .sorted((d1, d2) -> Double.compare(d1.getCurrentLoad(), d2.getCurrentLoad()))
                .findFirst();
    }

    //Test SSH connection to a device
    public boolean testConnection(Device device) {
        try {
            sshService.connectToDevice(device);
            sshService.disconnectFromDevice(device);
            return true;
        } catch (JSchException e) {
            log.error("Failed to connect to device {}: {}", device.getName(), e.getMessage());
            return false;
        }
    }

    public String getCpuMetrics(Device device) throws JSchException {
        SSHService.CommandResult result = sshClientManager.executeCommand(
                device,
                "top -bn1 | grep 'Cpu(s)' | awk '{print $2 + $4}'"
        );
        return result.isSuccess() ? result.getOutput().trim() : "Error fetching CPU metrics";
    }

    public String getMemoryMetrics(Device device) throws JSchException {
        SSHService.CommandResult result = sshClientManager.executeCommand(
                device,
                "free -m | grep Mem | awk '{print $3*100/$2}'"
        );
        return result.isSuccess() ? result.getOutput().trim() + "%" : "Error fetching memory metrics";
    }

    public String getGpuMetrics(Device device) throws JSchException {
        SSHService.CommandResult result = sshClientManager.executeCommand(
                device,
                "nvidia-smi --query-gpu=utilization.gpu,memory.used,memory.total --format=csv,noheader"
        );
        return result.isSuccess() ? result.getOutput().trim() : "Error fetching GPU metrics";
    }

    public String getSystemLoadMetrics(Device device) throws JSchException {
        SSHService.CommandResult result = sshClientManager.executeCommand(
                device,
                "uptime | awk -F'average:' '{print $2}'"
        );
        return result.isSuccess() ? result.getOutput().trim() : "Error fetching system load metrics";
    }

    public List<Map<String, Object>> continuousMonitor(Integer deviceId, int intervalSeconds, int durationSeconds) {
        List<Map<String, Object>> results = new ArrayList<>();
        Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new DeviceNotFoundException());

        // 计算需要监控的次数
        int times = durationSeconds / intervalSeconds;
        log.info("开始持续监控设备 {}, 间隔{}秒, 持续{}秒, 共{}次", device.getName(), intervalSeconds, durationSeconds, times);

        try {
            // 先连接设备
            sshService.connectToDevice(device);
            log.info("已连接到设备 {}", device.getName());

            for (int i = 0; i < times; i++) {
                try {
                    Map<String, Object> metrics = new HashMap<>();
                    metrics.put("timestamp", LocalDateTime.now().toString());
                    metrics.put("device_name", device.getName());
                    metrics.put("device_status", device.getStatus().toString());
                    metrics.put("index", i + 1);
                    metrics.put("total", times);

                    // 获取各项指标
                    String cpuUsage = "N/A";
                    String memoryUsage = "N/A";
                    String gpuMetrics = "N/A";
                    String systemLoad = "N/A";

                    try {
                        cpuUsage = getCpuMetrics(device);
                        metrics.put("cpu_usage", cpuUsage);
                    } catch (Exception e) {
                        log.error("获取CPU指标失败: {}", e.getMessage());
                        metrics.put("cpu_usage", "Error: " + e.getMessage());
                    }

                    try {
                        memoryUsage = getMemoryMetrics(device);
                        metrics.put("memory_usage", memoryUsage);
                    } catch (Exception e) {
                        log.error("获取内存指标失败: {}", e.getMessage());
                        metrics.put("memory_usage", "Error: " + e.getMessage());
                    }

                    try {
                        gpuMetrics = getGpuMetrics(device);
                        metrics.put("gpu_metrics", gpuMetrics);
                    } catch (Exception e) {
                        log.error("获取GPU指标失败: {}", e.getMessage());
                        metrics.put("gpu_metrics", "Error: " + e.getMessage());
                    }

                    try {
                        systemLoad = getSystemLoadMetrics(device);
                        metrics.put("system_load", systemLoad);
                    } catch (Exception e) {
                        log.error("获取系统负载指标失败: {}", e.getMessage());
                        metrics.put("system_load", "Error: " + e.getMessage());
                    }

                    // 更新设备状态
                    device.setLastHeartbeat(LocalDateTime.now());
                    deviceRepository.save(device);

                    results.add(metrics);

                    // 打印详细的监控信息
                    log.info("第{}次监控设备{}完成 - CPU: {}, 内存: {}, GPU: {}, 系统负载: {}",
                            i + 1, device.getName(), cpuUsage, memoryUsage, gpuMetrics, systemLoad);

                    // 如果不是最后一次，则等待指定的间隔时间
                    if (i < times - 1) {
                        Thread.sleep(intervalSeconds * 1000);
                    }
                } catch (Exception e) {
                    log.error("监控设备{}失败: {}", device.getName(), e.getMessage());
                    Map<String, Object> errorMetrics = new HashMap<>();
                    errorMetrics.put("timestamp", LocalDateTime.now().toString());
                    errorMetrics.put("error", "监控失败: " + e.getMessage());
                    results.add(errorMetrics);
                }
            }

            // 最后断开连接
            sshService.disconnectFromDevice(device);
            log.info("已断开与设备 {} 的连接", device.getName());

        } catch (Exception e) {
            log.error("持续监控设备{}失败: {}", device.getName(), e.getMessage());
            Map<String, Object> errorMetrics = new HashMap<>();
            errorMetrics.put("timestamp", LocalDateTime.now().toString());
            errorMetrics.put("error", "连接设备失败: " + e.getMessage());
            results.add(errorMetrics);
        }

        return results;
    }
}