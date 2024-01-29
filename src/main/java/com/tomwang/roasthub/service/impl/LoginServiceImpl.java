package com.tomwang.roasthub.service.impl;

import com.alibaba.fastjson.JSON;
import com.tomwang.roasthub.dao.pojo.Users;
import com.tomwang.roasthub.service.LoginService;
import com.tomwang.roasthub.service.SysUserService;
import com.tomwang.roasthub.utils.JWTUtils;
import com.tomwang.roasthub.vo.ErrorCode;
import com.tomwang.roasthub.vo.Result;
import com.tomwang.roasthub.vo.params.LoginParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


@Service
@Transactional
public class LoginServiceImpl implements LoginService {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    //加密盐用于加密
    private static final String slat = "tomwang!@#";
    @Override
    public Result login(LoginParam loginParam) {
        /**
         * 1、检测参数是否合法
         * 2、根据用户名和密码去user表中查询是否存在
         * 3、如果不存在就登陆失败
         * 4、如果存在，使用jwt生成token返回给前端
         * 5、token放入redis当中，redis token：user信息 设置过期时间
         * （登陆的时候先认证字符串是否合法，去redis认证是否存在）
         *
         */
        String account = loginParam.getUsername();
        String password = loginParam.getPassword();
        if (StringUtils.isBlank(account) || StringUtils.isBlank(password)) {
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(),ErrorCode.PARAMS_ERROR.getMsg());

        }
        password = DigestUtils.md5Hex(password+slat);
        Users user = sysUserService.findUser(account,password);
        if (user == null) {
            return Result.fail(ErrorCode.ACCOUNT_PWD_NOT_EXIST.getCode(),ErrorCode.ACCOUNT_PWD_NOT_EXIST.getMsg());
        }
        String token = JWTUtils.createToken((long) user.getId());
        // JSON.toJSONString 用法    https://blog.csdn.net/antony9118/article/details/71023009
        //过期时间是一百天
        //redisTemplate用法  https://blog.csdn.net/lydms/article/details/105224210
        redisTemplate.opsForValue().set("TOKEN_"+token, JSON.toJSONString(user),100, TimeUnit.DAYS);
        return Result.success(token);
    }

    @Override
    public Users checkToken(String token) {
        //token为空返回null
        if(StringUtils.isBlank(token) || StringUtils.isEmpty(token) ){
            return null;
        }
        System.out.println(token);
        Map<String, Object> stringObjectMap = JWTUtils.checkToken(token);
        //解析失败
        if(stringObjectMap ==null){
            return null;
        }
        //如果成功
        String userJson =  redisTemplate.opsForValue().get("TOKEN_"+token);
        if (StringUtils.isBlank(userJson)) {
            return null;
        }
        //解析回sysUser对象
        Users user = JSON.parseObject(userJson, Users.class);
        return user;
    }

    @Override
    public Result logout(String token) {
        redisTemplate.delete("TOken_"+token);
        return Result.success(null);
    }

    @Override
    public Result register(LoginParam loginParam) {
        /**
         * 1、判断参数是否合法
         * 2、判断账号是否存在，如果存在，返回账号已经被注册
         * 3、账号不存在，注册用户
         * 4、生成token
         * 5、存入redis并返回
         * 6、注意加上事务，一旦中间的任何过程出现
         */
        System.out.println("register "+loginParam);
        String username = loginParam.getUsername();
        String password = loginParam.getPassword();
        if (StringUtils.isBlank(username)
                || StringUtils.isBlank(password)
        ){
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(),ErrorCode.PARAMS_ERROR.getMsg());
        }
        Users user = sysUserService.findUserByAccount(username);

        if (user != null){
            return Result.fail(ErrorCode.ACCOUNT_EXIST.getCode(),ErrorCode.ACCOUNT_EXIST.getMsg());
        }
        user = new Users();
        user.setUsername(username);
        user.setPassword(DigestUtils.md5Hex(password+slat));
        this.sysUserService.save(user);

        String token = JWTUtils.createToken((long) user.getId());

        redisTemplate.opsForValue().set("TOKEN_"+token, JSON.toJSONString(user),100, TimeUnit.DAYS);
        return Result.success(token);

    }

    @Override
    public Result delete(Users user) {
        if (StringUtils.isBlank(user.getUsername())
                || StringUtils.isBlank(user.getPassword())
        ){
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(),ErrorCode.PARAMS_ERROR.getMsg());
        }
        Users tempUser = sysUserService.findUserByAccount(user.getUsername());

        if (tempUser != null){
            this.sysUserService.delete(user);
        }
        return Result.success("delete success");
    }

    //跑出新的密码
    public static void main(String[] args) {
        String password;
        password = "admin";
        password = DigestUtils.md5Hex(password+slat);
        System.out.println(password);
    }
}