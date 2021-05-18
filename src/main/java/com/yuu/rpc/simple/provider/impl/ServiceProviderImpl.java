package com.yuu.rpc.simple.provider.impl;

import com.yuu.rpc.simple.provider.ServiceProvider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServiceProviderImpl implements ServiceProvider {

    private Map<String, Object> serviceMap = new HashMap<>();

    @Override
    public void addService(Object obj) {
        // 确定serviceName
        String serviceName = obj.getClass().getInterfaces()[0].getCanonicalName();
        // 将关系映射存入map
        if (serviceMap.containsKey(serviceName)) {
            return;
        }
        serviceMap.put(serviceName, obj);
    }

    @Override
    public Object getService(String serviceName) {
        Object obj = serviceMap.get(serviceName);
        return obj;
    }
}
