package com.tomwang.roasthub.handler;


import com.tomwang.roasthub.vo.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

//对加了@controller注解的方法进行拦截处理 Aop的实现
@ControllerAdvice
public class AllExceptionHandler {

    //进行异常处理，处理Exception.class的异常
    @ExceptionHandler(Exception.class)
    @ResponseBody //返回json数据如果不加就返回页面了
    public Result doException(Exception ex) {
        //e.printStackTrace();是打印异常的堆栈信息，指明错误原因，
        // 其实当发生异常时，通常要处理异常，这是编程的好习惯，所以e.printStackTrace()可以方便你调试程序！
        ex.printStackTrace();
        return Result.fail(-999,"系统异常");

    }
}
