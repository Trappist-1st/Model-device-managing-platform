package com.templar.springboot_testdemo3.controller;

import com.templar.springboot_testdemo3.entity.User;
import com.templar.springboot_testdemo3.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    public User createUser(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam("role") String role) {
        return userService.createUser(username, password, role);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable String id) {
        return userService.findById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable String id) {
        userService.deleteById(id);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable String id,
                           @RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam("role") String role){
        return userService.updateUser(id, username, password, role);
    }
}
