package com.tomwang.roasthub.controller;

import com.tomwang.roasthub.dao.pojo.RegistrationRequest;
import com.tomwang.roasthub.dao.pojo.Users;
import com.tomwang.roasthub.service.LoginService;
import com.tomwang.roasthub.service.impl.RabbitMQSender;
import com.tomwang.roasthub.vo.Result;
import com.tomwang.roasthub.vo.params.LoginParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class LoginController {
    @Autowired
    private LoginService loginService;
    @Autowired
    private RabbitMQSender rabbitMQSender;
    private static final org.slf4j.Logger log
            = org.slf4j.LoggerFactory.getLogger(LoginController.class);

    @RequestMapping(value = "/login")
    @PostMapping
    public Result login(@RequestParam Map<String, String> params){
        LoginParam loginParam = new LoginParam();
        loginParam.setPassword((String) params.get("password"));
        loginParam.setUsername((String) params.get("username"));
        return loginService.login(loginParam);
    }

    @RequestMapping(value = "/logout")
    @PostMapping
    public Result logout(@RequestParam("token") String token){
        return loginService.logout(token);
    }

    @PostMapping
    @RequestMapping(value = "/register")
    public Result register(@RequestParam Map<String, String> params) {
        LoginParam loginParam = new LoginParam();
        loginParam.setPassword(params.get("password"));
        loginParam.setUsername(params.get("username"));
        Result res = loginService.register(loginParam);

        if (res.getMsg().equals("success")) {
            try {
                RegistrationRequest request = new RegistrationRequest();
                request.setPassword(params.get("password"));
                request.setEmail(params.get("username"));
                rabbitMQSender.send(request);
            } catch (Exception ex) {
                // 记录日志，处理异常
                Users user = new Users();
                user.setPassword(params.get("password"));
                user.setUsername(params.get("username"));
                loginService.delete(user);
                res.setMsg("Registration failed due to mail.properties sending failure.");
                res.setCode(500);
                res.setSuccess(false);
                res.setData(null);
            }
        }

        return res;
    }
}
