package com.macro.mall.util;

import com.macro.mall.model.UmsAdmin;
import org.springframework.stereotype.Component;

@Component
public class UserUtils {
    private static ThreadLocal<UmsAdmin> userDetail = new ThreadLocal<>();

    public static void setUserDetail(UmsAdmin umsMember){
        userDetail.set(umsMember);
    }

    public static void removeUserDetail(){
        userDetail.remove();
    }

    public static UmsAdmin getUserDetail(){
        return userDetail.get();
    }
}
