package com.macro.mall.portal.util;

import com.macro.mall.model.UmsMember;
import org.springframework.stereotype.Component;

@Component
public class UserUtils {
    private static ThreadLocal<UmsMember> userDetail = new ThreadLocal<>();

    public static void setUserDetail(UmsMember umsMember){
        userDetail.set(umsMember);
    }

    public static void removeUserDetail(){
        userDetail.remove();
    }

    public static UmsMember getUserDetail(){
        return userDetail.get();
    }
}
