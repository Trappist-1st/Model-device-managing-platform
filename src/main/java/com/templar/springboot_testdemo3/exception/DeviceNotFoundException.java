package com.templar.springboot_testdemo3.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DeviceNotFoundException extends RuntimeException{
    public DeviceNotFoundException(){
        super("Device not found");
    }
}
