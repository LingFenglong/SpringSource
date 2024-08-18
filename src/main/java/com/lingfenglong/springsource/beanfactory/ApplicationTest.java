package com.lingfenglong.springsource.beanfactory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListenerFactory;

public class ApplicationTest {

    @Test
    void beanDefinition() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        // RootBeanDefinition usernameBeanDefinition = new RootBeanDefinition(String.class);
        // usernameBeanDefinition.setFactoryBeanName("username");
        // usernameBeanDefinition.setScope("singleton");
        // usernameBeanDefinition.getConstructorArgumentValues()
        //         .addIndexedArgumentValue(0, "LingFengLong");

        // BeanDefinition usernameBeanDefinition = BeanDefinitionBuilder.genericBeanDefinition(String.class)
        //         .setScope("singleton")
        //         .addConstructorArgValue("LingFenglong")
        //         .getBeanDefinition();

        BeanDefinition usernameBeanDefinition = BeanDefinitionBuilder.rootBeanDefinition(String.class)
                .addConstructorArgValue("LingFenglong")
                .setScope("singleton")
                .getBeanDefinition();

        beanFactory.registerBeanDefinition("username", usernameBeanDefinition);

        String username = beanFactory.getBean("username", String.class);
        System.out.println("username = " + username);
    }


    @Test
    void configAndAutowire() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        BeanDefinition beanDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(BeanConfiguration.class)
                .getBeanDefinition();
        beanFactory.registerBeanDefinition("beanConfiguration", beanDefinition);

        BeanDefinition usernameBeanDefinition = BeanDefinitionBuilder.rootBeanDefinition(String.class)
                .addConstructorArgValue("LingFenglong")
                .setScope("singleton")
                .getBeanDefinition();
        beanFactory.registerBeanDefinition("username", usernameBeanDefinition);

        // 注册注解配置处理器
        // org.springframework.context.annotation.internalConfigurationAnnotationProcessor
        // org.springframework.context.annotation.internalAutowiredAnnotationProcessor
        // org.springframework.context.annotation.internalCommonAnnotationProcessor
        // org.springframework.context.event.internalEventListenerProcessor
        // org.springframework.context.event.internalEventListenerFactory
        AnnotationConfigUtils.registerAnnotationConfigProcessors(beanFactory);

        // org.springframework.context.annotation.internalAutowiredAnnotationProcessor
        // org.springframework.context.annotation.internalCommonAnnotationProcessor
        beanFactory.getBeansOfType(BeanPostProcessor.class)
                .entrySet()
                .stream()
                .sorted(beanFactory.getDependencyComparator())  // 排序后置处理器
                .forEach(entry -> {
                    beanFactory.addBeanPostProcessor(entry.getValue());    // autowire
                    System.out.println(entry.getKey());
                });

        System.out.println();

        // org.springframework.context.annotation.internalConfigurationAnnotationProcessor
        // org.springframework.context.event.internalEventListenerProcessor
        beanFactory.getBeansOfType(BeanFactoryPostProcessor.class)
                .forEach((k, v) -> {
                    v.postProcessBeanFactory(beanFactory);  // auto config
                    System.out.println(k);
                });

        System.out.println();

        // org.springframework.context.event.internalEventListenerFactory
        beanFactory.getBeansOfType(EventListenerFactory.class)
                .forEach((k, v) -> System.out.println(k));


        // 提前加载，默认懒加载
        beanFactory.preInstantiateSingletons();

        // test config
        beanFactory.getBeansOfType(Object.class)
                .forEach((k, v) -> System.out.println(k));

        // test autowire
        BeanConfiguration beanConfiguration = beanFactory.getBean(BeanConfiguration.class);
        System.out.println("beanConfiguration.username = " + beanConfiguration.username);
    }

    @Configuration
    static class BeanConfiguration {
        @Autowired
        private String username;

        @Bean
        public BeanA beanA(BeanB beanB) {
            return new BeanA(beanB);
        }

        @Bean
        public BeanB beanB() {
            return new BeanB();
        }
    }

    record BeanA(BeanB beanB) {

    }

    record BeanB() {

    }
}
