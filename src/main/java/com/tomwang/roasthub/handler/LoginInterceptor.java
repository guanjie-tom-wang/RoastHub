package com.tomwang.roasthub.handler;

import com.alibaba.fastjson.JSON;
import com.tomwang.roasthub.dao.pojo.Users;
import com.tomwang.roasthub.service.LoginService;
import com.tomwang.roasthub.utils.UserThreadLocal;
import com.tomwang.roasthub.vo.ErrorCode;
import com.tomwang.roasthub.vo.Result;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

//https://blog.csdn.net/fansili/article/details/78740877   @Component, @Repository, @Service的区别

@Component
@Slf4j//日志
public class LoginInterceptor implements HandlerInterceptor {


    @Autowired
    private LoginService loginService;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //在执行controller方法(在springMVC中叫做handler）之前进行执行
        /**
         *1、需要判断请求的接口路径是否为HandlerMethod，判断确实为controller的方法
         * 2、判断token是否为空，如果为空，未登录
         * 3、如果token不为空，进行登陆验证 loginservice checkToken
         * 4、如果认证成功，放行
         */
        //如果不是我们的方法进行放行
        if (!(handler instanceof HandlerMethod) ){
            //handler 可能是访问资源的RequestResourceHandler springboot程序访问静态资源默认去classpath下的static目录去查询
            return true;
        }
        String token = request.getHeader("Authorization");

        log.info("=================request start===========================");
        String requestURI = request.getRequestURI();
        log.info("request uri:{}",requestURI);
        log.info("request method:{}",request.getMethod());
        log.info("token:{}", token);
        log.info("=================request end===========================");

        if(StringUtils.isBlank(token)){
            Result result = Result.fail(ErrorCode.NO_LOGIN.getCode(), "未登录");
            //设置浏览器识别返回的是json
            response.setContentType("application/json;charset=utf-8");
            //https://www.cnblogs.com/qlqwjy/p/7455706.html response.getWriter().print()
            //SON.toJSONString则是将对象转化为Json字符串
            response.getWriter().print(JSON.toJSONString(result));
            return false;
        }
        Users user = loginService.checkToken(token);
        if (user == null) {
            Result result = Result.fail(ErrorCode.NO_LOGIN.getCode(), "未登录");
            //设置浏览器识别返回的是json
            response.setContentType("application/json;charset=utf-8");
            //https://www.cnblogs.com/qlqwjy/p/7455706.html response.getWriter().print()
            //SON.toJSONString则是将对象转化为Json字符串
            response.getWriter().print(JSON.toJSONString(result));
            return false;

        }
        //登陆验证成功，放行
        //我希望在controller中直接获取用户的信息怎么获取
        UserThreadLocal.put(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //如果不删 ThreadLocal中用完的信息会有内存泄漏的风险
        UserThreadLocal.remove();
    }
}
