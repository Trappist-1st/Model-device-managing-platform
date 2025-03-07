package com.templar.springboot_testdemo3.controller;

import com.templar.springboot_testdemo3.entity.User;
import com.templar.springboot_testdemo3.service.TokenService;
import com.templar.springboot_testdemo3.service.UserService;
import com.templar.springboot_testdemo3.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.Data;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private VerificationCodeService verificationCodeService;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/code")
    public ResponseEntity<?> sendVerificationCode(@RequestParam String username) {
        Optional<User> user = userService.findByUsername(username);
        if (user.isPresent()) {
            String code = verificationCodeService.generateCode(username);
            // 这里应该调用短信服务发送验证码，现在只是返回验证码
            return ResponseEntity.ok().body(new CodeResponse(code));
        }
        return ResponseEntity.badRequest().body("用户不存在");
    }

    @PostMapping("/code-login")
    public ResponseEntity<?> loginWithCode(@RequestBody CodeLoginRequest request) {
        if (!verificationCodeService.verifyCode(request.getUsername(), request.getCode())) {
            return ResponseEntity.badRequest().body("验证码错误或已过期");
        }

        Optional<User> foundUser = userService.findByUsername(request.getUsername());
        if (!foundUser.isPresent()) {
            //return ResponseEntity.badRequest().body("用户名或密码错误");
            //自动注册新用户
            User newUser = userService.createUser(request.getUsername(), null,"user");
            if(newUser == null){
                return ResponseEntity.badRequest().body("自动注册失败");
            }
            foundUser = Optional.of(newUser);
        }

        //生成令牌
        String token=tokenService.generateToken(foundUser.get().getUsername());
        return ResponseEntity.ok().body(new AuthResponse(true, foundUser.get(),"登录成功", token));
    }

    //密码登录
    @PostMapping("/password-login")
    public ResponseEntity<?> loginWithPassword(@RequestBody PasswordLoginRequest request){
        Optional<User> foundUser = userService.findByUsername(request.getUsername());
        if(foundUser.isEmpty()){
            return ResponseEntity.badRequest().body("用户不存在");
        }
        if(!userService.validatePassword(foundUser.get(),request.getPassword())){
            return ResponseEntity.badRequest().body("密码错误");
        }

        String token = tokenService.generateToken(foundUser.get().getUsername());
        return ResponseEntity.ok().body(new AuthResponse(true, foundUser.get(),"登录成功", token));
    }

    @Data
    private static class CodeLoginRequest {
        private String username;
        private String code;

        public String getUsername() {
            return username;
        }

        public String getCode() {
            return code;
        }
    }

    @Data
    private static class PasswordLoginRequest{
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }

    @Data
    private static class CodeResponse {
        private final String code;
        
        public CodeResponse(String code) {
            this.code = code;
        }
    }

    @Data
    private static class AuthResponse {
        private final boolean success;
        private final String message;
        private final User user;
        private final String token;

        public AuthResponse(boolean success,User user,String message,String token) {
            this.success = true;
            this.user = user;
            this.message=message;
            this.token = token;
        }
    }
}
