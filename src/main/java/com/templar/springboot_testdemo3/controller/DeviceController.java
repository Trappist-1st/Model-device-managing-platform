package com.templar.springboot_testdemo3.controller;

import com.jcraft.jsch.JSchException;
import com.templar.springboot_testdemo3.entity.Device;
import com.templar.springboot_testdemo3.exception.DeviceNotFoundException;
import com.templar.springboot_testdemo3.repository.DeviceRepository;
import com.templar.springboot_testdemo3.service.DeviceMonitorService;
import com.templar.springboot_testdemo3.service.DeviceService;
import com.templar.springboot_testdemo3.service.SSHService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private DeviceMonitorService deviceMonitorService;

    @Autowired
    private SSHService sshService;

    @Autowired
    private DeviceRepository deviceRepository;

    @PostMapping
    public Device createDevice(@RequestBody Device device){
        return deviceService.createDevice(device);
    }

    @GetMapping
    public List<Device> findAllDevices(){
        return deviceService.getAllDevices();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable Integer id){
        Device device= deviceService.getDeviceById(id)
                .orElse(null);
        return ResponseEntity.ok(device);
    }

    @GetMapping("/available")
    public ResponseEntity<List<Device>> getAvailableDevices(){
        return ResponseEntity.ok(deviceService.getAvailableDevices());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Device> updateDevice(@PathVariable Integer id, @RequestBody Device device){
        return deviceService.getDeviceById(id)
                .map(existingDevice->{
                    if (device.getGpuInfo() == null) device.setGpuInfo(existingDevice.getGpuInfo());
                    if (device.getLastHeartbeat() == null) device.setLastHeartbeat(existingDevice.getLastHeartbeat());
                    if (device.getGpuCount() == 0) device.setGpuCount(existingDevice.getGpuCount());
                    if (device.getCurrentLoad() == null) device.setCurrentLoad(existingDevice.getCurrentLoad());
                    if (device.getLastConnected() == null) device.setLastConnected(existingDevice.getLastConnected());
                    if (device.getSshKeyPath() == null) device.setSshKeyPath(existingDevice.getSshKeyPath());

                    device.setId(id);
                    return ResponseEntity.ok(deviceService.registerDevice(device));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public void deleteDevice(@PathVariable Integer id) {
        deviceService.deleteById(id);
    }

    @PutMapping("/{id}/status")
    public Device updateDeviceStatus(@PathVariable Integer id,@RequestParam Device.DeviceStatus status){
        return deviceService.updateDeviceStatus(id,status);
    }

    @GetMapping("/status/{status}")
    public List<Device> getDevicesById(@PathVariable Device.DeviceStatus status){
        return deviceService.findByStatus(status);
    }

    @PostMapping("/{id}/health-check")
    public void checkDeviceHealth(@PathVariable Integer id) throws JSchException {
        deviceService.checkDeviceHealth(id);
    }

    @GetMapping("/{id}/heartbeat")
    public LocalDateTime getLastHeartbeat(@PathVariable Integer id) {
        return deviceService.getLastHeartbeat(id);
    }

    @GetMapping("/{id}/test-connection")
    public ResponseEntity<Map<String, Boolean>> testConnection(@PathVariable Integer id) {
        return deviceService.getDeviceById(id)
                .map(device -> {
                    boolean result = deviceService.testConnection(device);
                    return ResponseEntity.ok(Map.of("connected", result));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    //获取设备CPU使用率
    @GetMapping("/{id}/cpu-metrics")
    public ResponseEntity<Map<String,String>> getCpuMetrics(@PathVariable Integer id){
        return deviceService.getDeviceById(id)
                .map(device -> {
                    try {
                        String cpuMetrics = deviceService.getCpuMetrics(device);
                        return ResponseEntity.ok(Map.of("cpu_usage", cpuMetrics));
                    } catch (JSchException e) {
                        return ResponseEntity.ok(Map.of("error","Failed yo get CPU metrics: "+e.getMessage()));
                    }
                }).orElse(ResponseEntity.notFound().build());
    }

    //获取设备内存使用率
    @GetMapping("/{id}/metrics/memory")
    public ResponseEntity<Map<String, String>> getMemoryMetrics(@PathVariable Integer id) {
        return deviceService.getDeviceById(id)
                .map(device -> {
                    try {
                        String memoryMetrics = deviceService.getMemoryMetrics(device);
                        return ResponseEntity.ok(Map.of("memory_usage", memoryMetrics));
                    } catch (JSchException e) {
                        return ResponseEntity.ok(Map.of("error", "Failed to get memory metrics: " + e.getMessage()));
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    //获取设备GPU使用率
    @GetMapping("/{id}/metrics/gpu")
    public ResponseEntity<Map<String, String>> getGpuMetrics(@PathVariable Integer id) {
        return deviceService.getDeviceById(id)
                .map(device -> {
                    try {
                        String gpuMetrics = deviceService.getGpuMetrics(device);
                        return ResponseEntity.ok(Map.of("gpu_metrics", gpuMetrics));
                    } catch (JSchException e) {
                        return ResponseEntity.ok(Map.of("error", "Failed to get GPU metrics: " + e.getMessage()));
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    //获取设备负载情况
    @GetMapping("/{id}/metrics/load")
    public ResponseEntity<Map<String, String>> getSystemLoadMetrics(@PathVariable Integer id) {
        return deviceService.getDeviceById(id)
                .map(device -> {
                    try {
                        String loadMetrics = deviceService.getSystemLoadMetrics(device);
                        return ResponseEntity.ok(Map.of("system_load", loadMetrics));
                    } catch (JSchException e) {
                        return ResponseEntity.ok(Map.of("error", "Failed to get system load metrics: " + e.getMessage()));
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/monitor")
    public ResponseEntity<String> monitorDevice(@PathVariable Integer id){
        return deviceService.getDeviceById(id)
                .map(device->{
                    try{
                        deviceMonitorService.collectDeviceMetrics(device);
                        return ResponseEntity.ok("Device metrics collected successfully");
                    } catch (JSchException e) {
                        return ResponseEntity.internalServerError().body("Failed to collect device metrics: "+e.getMessage());
                    }
                }).orElse(ResponseEntity.notFound().build());
    }

    //获取设备的所有指标
    @GetMapping("/{id}/metrics")
    public ResponseEntity<Map<String, Object>> getDeviceMetrics(@PathVariable Integer id) {
        return deviceService.getDeviceById(id)
                .map(device -> {
                    Map<String, Object> metrics = new HashMap<>();
                    try {
                        metrics.put("cpu_usage", deviceService.getCpuMetrics(device));
                        metrics.put("memory_usage", deviceService.getMemoryMetrics(device));
                        metrics.put("gpu_metrics", deviceService.getGpuMetrics(device));
                        metrics.put("system_load", deviceService.getSystemLoadMetrics(device));
                        metrics.put("device_name", device.getName());
                        metrics.put("device_status", device.getStatus().toString());
                        metrics.put("last_heartbeat", device.getLastHeartbeat() != null ?
                                device.getLastHeartbeat().toString() : "N/A");
                        metrics.put("timestamp", LocalDateTime.now().toString());
                        return ResponseEntity.ok(metrics);
                    } catch (JSchException e) {
                        metrics.put("error", "获取设备指标失败: " + e.getMessage());
                        return ResponseEntity.ok(metrics);
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/continuous-monitor")
    public ResponseEntity<List<Map<String, Object>>> continuousMonitor(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "1") int interval,
            @RequestParam(defaultValue = "60") int duration) {
        try {
            log.info("开始持续监控设备 {}, 间隔 {} 秒, 持续 {} 秒", id, interval, duration);
            List<Map<String, Object>> results = deviceService.continuousMonitor(id, interval, duration);
            log.info("持续监控设备 {} 完成, 共获取 {} 次数据", id, results.size());
            return ResponseEntity.ok(results);
        } catch (DeviceNotFoundException e) {
            log.error("设备 {} 不存在", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("持续监控设备 {} 失败: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/continuous-monitor1")
    public ResponseEntity<?> continuousMonitor(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "1") int interval,
            @RequestParam(defaultValue = "60") int duration,
            @RequestParam(defaultValue = "true") boolean realtime) {

        if (realtime) {
            // 使用SseEmitter实现实时数据流
            SseEmitter emitter = new SseEmitter(duration * 1000L + 5000L);

            // 在新线程中执行监控，避免阻塞主线程
            new Thread(() -> {
                try {
                    Device device = deviceService.getDeviceById(id)
                            .orElseThrow(() -> new DeviceNotFoundException());

                    // 先连接设备
                    sshService.connectToDevice(device);

                    // 发送开始监控的消息
                    emitter.send(SseEmitter.event()
                            .name("start")
                            .data(Map.of(
                                    "message", "开始监控设备: " + device.getName(),
                                    "device", device.getName(),
                                    "interval", interval,
                                    "duration", duration,
                                    "times", duration / interval
                            )));

                    // 计算需要监控的次数
                    int times = duration / interval;

                    for (int i = 0; i < times; i++) {
                        try {
                            Map<String, Object> metrics = new HashMap<>();
                            metrics.put("timestamp", LocalDateTime.now().toString());
                            metrics.put("device_name", device.getName());
                            metrics.put("device_status", device.getStatus().toString());
                            metrics.put("index", i + 1);
                            metrics.put("total", times);

                            // 获取各项指标
                            try {
                                String cpuUsage = deviceService.getCpuMetrics(device);
                                metrics.put("cpu_usage", cpuUsage);
                            } catch (Exception e) {
                                metrics.put("cpu_usage", "Error: " + e.getMessage());
                            }

                            try {
                                String memoryUsage = deviceService.getMemoryMetrics(device);
                                metrics.put("memory_usage", memoryUsage);
                            } catch (Exception e) {
                                metrics.put("memory_usage", "Error: " + e.getMessage());
                            }

                            try {
                                String gpuMetrics = deviceService.getGpuMetrics(device);
                                metrics.put("gpu_metrics", gpuMetrics);
                            } catch (Exception e) {
                                metrics.put("gpu_metrics", "Error: " + e.getMessage());
                            }

                            try {
                                String systemLoad = deviceService.getSystemLoadMetrics(device);
                                metrics.put("system_load", systemLoad);
                            } catch (Exception e) {
                                metrics.put("system_load", "Error: " + e.getMessage());
                            }

                            // 更新设备状态
                            device.setLastHeartbeat(LocalDateTime.now());
                            deviceRepository.save(device);

                            // 发送监控数据
                            emitter.send(SseEmitter.event()
                                    .name("metrics")
                                    .data(metrics));

                            // 如果不是最后一次，则等待指定的间隔时间
                            if (i < times - 1) {
                                Thread.sleep(interval * 1000);
                            }
                        } catch (Exception e) {
                            emitter.send(SseEmitter.event()
                                    .name("error")
                                    .data(Map.of(
                                            "message", "监控设备失败: " + e.getMessage(),
                                            "timestamp", LocalDateTime.now().toString()
                                    )));
                        }
                    }

                    // 断开连接
                    sshService.disconnectFromDevice(device);

                    // 发送完成消息
                    emitter.send(SseEmitter.event()
                            .name("complete")
                            .data(Map.of(
                                    "message", "监控完成",
                                    "timestamp", LocalDateTime.now().toString()
                            )));

                    // 完成
                    emitter.complete();

                } catch (DeviceNotFoundException e) {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("error")
                                .data(Map.of("message", "设备不存在")));
                        emitter.complete();
                    } catch (Exception ex) {
                        emitter.completeWithError(ex);
                    }
                } catch (Exception e) {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("error")
                                .data(Map.of("message", "监控失败: " + e.getMessage())));
                        emitter.complete();
                    } catch (Exception ex) {
                        emitter.completeWithError(ex);
                    }
                }
            }).start();

            return ResponseEntity.ok(emitter);
        } else {
            // 使用原有的非实时方式
            try {
                List<Map<String, Object>> results = deviceService.continuousMonitor(id, interval, duration);
                return ResponseEntity.ok(results);
            } catch (DeviceNotFoundException e) {
                return ResponseEntity.notFound().build();
            } catch (Exception e) {
                return ResponseEntity.internalServerError().build();
            }
        }
    }

}
