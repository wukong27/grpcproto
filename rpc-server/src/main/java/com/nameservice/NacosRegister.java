package com.nameservice;

import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Properties;

@Service
public class NacosRegister {

    @Value("${grpc.server.port}")
    private int grpcPort;
    @Value("${spring.application.name}")
    private String serverName;

    public void register(int port,String serverName) throws Exception {
        // 1. 获取本机IP（优先获取非回环地址）
        String ip = getLocalIp();
        System.out.println("Local IP: " + ip);
        // 2. 创建 Nacos NamingService
        Properties properties = new Properties();
        properties.put("serverAddr", "127.0.0.1:8848");

        NamingService namingService = NamingFactory.createNamingService(properties);

        // 3. 构造实例（推荐方式，比简单注册更灵活）
        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(port);
        instance.setWeight(1.0);
        instance.setHealthy(true);
        instance.setEphemeral(true); // 临时实例（自动心跳）

        // 可选：元数据
        instance.getMetadata().put("version", "v1");
        instance.getMetadata().put("env", "dev");

        // 4. 注册实例（自动心跳启动🔥）
        namingService.registerInstance(serverName, instance);

        System.out.println("Service registered to Nacos: " + serverName);

        // 5. 挂起，保持进程运行（否则心跳线程会结束）
        Thread.currentThread().join();

        // （可选）优雅下线
        // namingService.deregisterInstance(serviceName, ip, port);
    }

    /**
     * 获取本机IP（优先内网IP）
     */
    private static String getLocalIp() throws Exception {

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();

            // 过滤无效网卡
            if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                continue;
            }

            Enumeration<InetAddress> addresses = ni.getInetAddresses();

            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();

                if (!addr.isLoopbackAddress() && addr instanceof java.net.Inet4Address) {
                    return addr.getHostAddress();
                }
            }
        }

        // fallback
        return InetAddress.getLocalHost().getHostAddress();
    }

    @PostConstruct
    public void init() throws Exception {
        register(grpcPort,serverName);
    }

}