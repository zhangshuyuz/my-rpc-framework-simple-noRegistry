package com.yuu.rpc.simple.provider;

/**
 * store and provide service object.
 *
 * @author shuang.kou
 * @createTime 2020年05月31日 16:52:00
 */
public interface ServiceProvider {

    void addService(Object obj);

    Object getService(String serviceName);


}
