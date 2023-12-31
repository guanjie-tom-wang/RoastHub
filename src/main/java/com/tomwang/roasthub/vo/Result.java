package com.tomwang.roasthub.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

//@Data这个注解是自动添加各属性的get，set方法，而@AllArgsConstructor这个注解是自动添加类的构造函数
@Data
@AllArgsConstructor
public class Result {

    private boolean success;

    private int code;

    private String msg;

    private Object data;



    public static Result fail(int code,String msg) {
        return new Result(false,code,msg,null);
    }

    public static Result success(Object data) {
        return new Result(true,200,"success",data);
    }




}
