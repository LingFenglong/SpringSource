package com.lingfenglong.springsource.aware;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.support.GenericApplicationContext;

public class ApplicationTest {
    @Test
    public void awareTest() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean(Bean1.class);

        context.refresh();
        context.close();
    }

    @Test
    public void autowireNotWorkTest() {
        GenericApplicationContext context = new GenericApplicationContext();

        context.registerBean(ConfigurationClassPostProcessor.class);
        context.registerBean(AutowiredAnnotationBeanPostProcessor.class);
        context.registerBean(CommonAnnotationBeanPostProcessor.class);
        context.registerBean(Config.class);

        context.refresh();
        context.close();
    }

    /*
    ----- Aware -----
    PostConstruct
    Initializing Bean
    initMethod
    Destroy
    Disposable Bean
    destroyMethod
     */
    @Test
    public void initAndDestroyTest() {
        GenericApplicationContext context = new GenericApplicationContext();

        context.registerBean(ConfigurationClassPostProcessor.class);
        context.registerBean(CommonAnnotationBeanPostProcessor.class);
        context.registerBean(InitAndDestroyConfig.class);

        context.refresh();
        context.close();
    }
}

@Configuration
class Config {
    @Autowired
    public void applicationContextAware(ApplicationContext applicationContext) {
        System.out.println(applicationContext);
    }

    @Bean
    public String message() {
        System.out.println("message");
        return "Hello world!";
    }

    @PostConstruct
    public void init() {
        System.out.println("init");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("destroy");
    }

    /*
    优先实例化 Config 类，而这时的其他 BeanFactoryPostProcessor 还没有准备好
    解决方式：
    1. 方法用 static 修饰
    2. 使用 Aware 接口
     */
    @Bean
    public BeanFactoryPostProcessor beanFactoryPostProcessor() {
        return beanFactory -> {
            System.out.println("beanFactoryPostProcessor");
        };
    }
}

class Bean1 implements BeanNameAware, ApplicationContextAware, BeanFactoryAware, BeanClassLoaderAware, InitializingBean {

    @Override
    public void setBeanName(String name) {
        System.out.println("Bean name is " + name);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("ApplicationContext is " + applicationContext);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        System.out.println("BeanClassLoader is " + classLoader);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("BeanFactory is " + beanFactory);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("Initializing Bean");
    }
}

@Configuration
class InitAndDestroyConfig {
    @Bean(initMethod = "initMethod", destroyMethod = "destroyMethod")
    public Bean2 bean2() {
        return new Bean2();
    }
}

class Bean2 implements InitializingBean, DisposableBean, BeanNameAware {
    // 初始化
    @PostConstruct
    public void postConstruct() {
        System.out.println("PostConstruct");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("Initializing Bean");
    }

    // 销毁
    @PreDestroy
    public void preDestroy() {
        System.out.println("Destroy");
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("Disposable Bean");
    }

    private void initMethod() {
        System.out.println("initMethod");
    }

    private void destroyMethod() {
        System.out.println("destroyMethod");
    }

    // Aware
    @Override
    public void setBeanName(String name) {
        System.out.println("----- Aware -----");
    }
}