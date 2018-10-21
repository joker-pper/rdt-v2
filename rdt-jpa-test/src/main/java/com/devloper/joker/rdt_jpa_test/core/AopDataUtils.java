package com.devloper.joker.rdt_jpa_test.core;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class AopDataUtils {

    public static Object getTargetInstance(ProxyFactory proxyFactory, Object target) {
        Object targetInstance = null;
        if (proxyFactory != null) {
            try {
                targetInstance = proxyFactory.getTargetSource().getTarget();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else targetInstance = target;
        return targetInstance;
    }

    //获取Spring代理的实际对象
    public static Object getTargetInstance(Object target) {
        if(target == null || !AopUtils.isAopProxy(target)) {
            //不是代理对象
            return target;
        }
        ProxyFactory proxyFactory = getProxyFactory(target);
        return getTargetInstance(getTargetInstance(proxyFactory, target));
    }

    public static Object getFieldValue(Object target, String fieldName) {
        Field field = ReflectionUtils.findField(target.getClass(), fieldName);
        field.setAccessible(true);
        return ReflectionUtils.getField(field, target);
    }

    //proxyObject为aop中连接点对象.getThis()
    public static ProxyFactory getProxyFactory(Object proxyObject) {
        ProxyFactory proxyFactory = null;
        Object advised = null;
        if (proxyObject != null) {
            if(AopUtils.isJdkDynamicProxy(proxyObject)) {
                InvocationHandler invo = Proxy.getInvocationHandler(proxyObject);
                advised = getFieldValue(invo, "advised");
            } else if (AopUtils.isCglibProxy(proxyObject)) {
                Object invo = getFieldValue(proxyObject, "CGLIB$CALLBACK_0");
                advised = getFieldValue(invo, "advised");
            }
            if (advised != null && advised instanceof ProxyFactory) {
                proxyFactory = (ProxyFactory) advised;
            }
        }
        return proxyFactory;
    }

}
