package com.templar.springboot_testdemo3.service;

import com.templar.springboot_testdemo3.entity.User;
import com.templar.springboot_testdemo3.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User createUser(String username, String rawPassword,String role){
        User user = new User();
        user.setUsername(username);
        user.setRole(role);

        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPassword(rawPassword);
        }

        return userRepository.save(user);
    }

    public List<User> findAll(){
        return userRepository.findAll();
    }

    public User findById(String id){
        return userRepository.findById(id).orElse(null);
    }

    public User updateUser(String id, String username, String password, String role){
        User user=userRepository.findById(id).orElse(null);
        if(user!=null){
            user.setUsername(username);
            user.setPassword(password);
            user.setRole(role);
            return userRepository.save(user);
        }else{
            return null;
        }
    }

    public void deleteById(String id){
        userRepository.deleteById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean validatePassword(User user,String rawPassword){
        return user.getPassword().equals(rawPassword);
    }
}
