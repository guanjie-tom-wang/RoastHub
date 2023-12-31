package com.tomwang.roasthub.utils;
import com.tomwang.roasthub.dao.pojo.Users;

public class UserThreadLocal {

    private UserThreadLocal() {
    }

    private static final ThreadLocal<Users> LOCAL =new ThreadLocal<Users>();
    //放入
    public static void put(Users user){
        LOCAL.set(user);
    }

    //取出
    public static Users get(){
        return LOCAL.get();
    }
    //删除
    public static void remove(){
        LOCAL.remove();
    }




}
