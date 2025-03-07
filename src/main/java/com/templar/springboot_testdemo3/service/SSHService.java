package com.templar.springboot_testdemo3.service;

import com.jcraft.jsch.*;
import com.templar.springboot_testdemo3.entity.Device;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;



@Slf4j
@Service
public class SSHService {

    @Value("${ssh.host}")
    private String host;

    @Value("${ssh.port}")
    private int port;

    @Value("${ssh.username}")
    private String username;

    @Value("${ssh.password}")
    private String password;

    @Value("${ssh.timeout:5000}")
    private int timeout;

    @Value("${ssh.retry:3}")
    private int retry;

    private final Map<Integer,Session> deviceSessions = new HashMap<>();

    //Connect to a device via SSH
    public Session connectToDevice(Device device) throws JSchException{
        log.info("Connecting to device: {} at {}:{}", device.getName(), device.getIpAddress(), device.getPort());
        JSch jsch = new JSch();

        if(device.getSshKeyPath()!=null && !device.getSshKeyPath().isEmpty()){
            jsch.addIdentity(device.getSshKeyPath());
        }

        Session session = jsch.getSession(device.getUsername(),device.getIpAddress(),device.getPort());
        if (device.getSshKeyPath() == null || device.getSshKeyPath().isEmpty()) {
            session.setPassword(device.getPassword());
        }

        Properties config = new Properties();
        config.put("StrictHostKeyChecking","no");
        session.setConfig(config);
        session.connect(30000);

        deviceSessions.put(device.getId(),session);

        log.info("Successfully connected to device:{ }",device.getName());
        return session;
    }

    //Disconnect from a device
    public void disconnectFromDevice(Device device){
        Session session = deviceSessions.get(device.getId());
        if(session!=null && session.isConnected()){
            session.disconnect();
            deviceSessions.remove(device.getId());
            log.info("Disconnected from device:{}",device.getName());
        }
    }

    //Execute a command on a device and return the result
    public CommandResult executeCommand(Device device,String command)throws JSchException{
        log.debug("Executing command on device {}:{}",host,port,command);
        
        Session session = deviceSessions.getOrDefault(device.getId(),null);
        if(session==null || !session.isConnected()){
            session = connectToDevice(device);
        }
        
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
                
        CommandResult result = new CommandResult();

        try(InputStream in = channel.getInputStream();
            InputStream err = channel.getErrStream()){
            channel.connect();

            result.setOutput(readInputStream(in));
            result.setError(readInputStream(err));
            result.setExitStatus(channel.getExitStatus());

            log.debug("Command result: exitStatus={}, output={}, error={}",
                    result.getExitStatus(), result.getOutput(), result.getError());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally{
            channel.disconnect();
        }

        return result;
    }

    private String readInputStream(InputStream in) throws IOException {
        StringBuilder output = new StringBuilder();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(in))){
            String line;
            while((line = reader.readLine())!=null){
                output.append(line).append("\n");
            }
        }
        return output.toString();
    }


    //结果封装类
    public static class CommandResult{
        private int exitStatus;
        private String output;
        private String error;

        public void setOutput(String output) {
            this.output = output;
        }

        public void setError(String error) {
            this.error = error;
        }

        public void setExitStatus(int exitStatus) {
            this.exitStatus = exitStatus;
        }

        public int getExitStatus() {
            return exitStatus;
        }

        public String getOutput() {
            return output;
        }

        public String getError() {
            return error;
        }

        public boolean isSuccess() {
            return exitStatus == 0;
        }
    }
}

