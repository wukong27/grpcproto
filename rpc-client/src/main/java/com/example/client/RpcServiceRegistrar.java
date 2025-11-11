package com.example.client;

import com.example.RpcService;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.io.File;

public class RpcServiceRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        // 你可以通过配置指定包名，这里写死为 com.example
        String basePackage = "com.example";

//        System.out.println("扫描路径：" + new File(ClassLoader.getSystemResource("").getPath()));

        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false) {
                    @Override
                    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                        // 放宽条件：接口也可以作为候选组件
                        AnnotationMetadata metadata = beanDefinition.getMetadata();
                        return metadata.isInterface() || metadata.isIndependent();
                    }
                };

        scanner.addIncludeFilter(new AnnotationTypeFilter(RpcService.class));

//        System.out.println("开始查找候选组件");
        scanner.findCandidateComponents(basePackage).forEach(candidate -> {
//            System.out.println("找到候选组件: " + candidate.getBeanClassName());
            try {
                Class<?> clazz = Class.forName(candidate.getBeanClassName());
                // 判断是否为接口
                if (!clazz.isInterface()) {
                    // 如果不是接口，则跳过或抛出异常
                    System.out.println("跳过非接口类: " + clazz.getName());
                    return;
                }

                BeanDefinitionBuilder builder =
                        BeanDefinitionBuilder.genericBeanDefinition(RpcProxyFactoryBean.class);
                builder.addConstructorArgValue(clazz);

                String beanName = clazz.getSimpleName();
                registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
                System.out.println("注册 RPC 代理 Bean: " + beanName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}