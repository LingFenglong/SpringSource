package com.lingfenglong.springsource.lifecycle;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

public class ApplicationTest {

    /*
     * postProcessBeforeInstantiation...
     * postProcessAfterInstantiation...
     * postProcessProperties...
     * postProcessBeforeInitialization...
     * postProcessAfterInitialization...
     * postProcessBeforeInstantiation...
     * constructor 1 LingFenglong
     * postProcessBeforeInitialization...
     * post construct annotation
     * InitializingBean afterPropertiesSet
     * postProcessAfterInitialization...
     * user = User[id=1, name=LingFenglong]
     * postProcessBeforeDestruction...
     * pre destroy annotation
     * DestroyBean destroy
     * postProcessBeforeDestruction...
     */
    @Test
    public void lifecycle() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MyConfig.class, User.class);

        context.getBeanFactory().addBeanPostProcessor(new MyBeanPostProcessor());

        User user = context.getBean(User.class);
        System.out.println("user = " + user);

        context.close();
    }

    static class MyConfig {
        @Bean
        static MyBeanPostProcessor myBeanPostProcessor() {
            return new MyBeanPostProcessor();
        }
    }

    @Component
    record User() implements InitializingBean, DisposableBean {
        @Autowired
        public void autowire(@Value("${JAVA_HOME}") String javaHome) {
            System.out.println("autowire" + javaHome);
        }

        public User {
            System.out.println("constructor");
        }

        @PostConstruct
        public void postConstruct() {
            System.out.println("post construct annotation");
        }

        @PreDestroy
        public void preDestroy() {
            System.out.println("pre destroy annotation");
        }

        @Override
        public void destroy() {
            System.out.println("DestroyBean destroy");
        }

        @Override
        public void afterPropertiesSet() {
            System.out.println("InitializingBean afterPropertiesSet");
        }
    }

    static class MyBeanPostProcessor implements InstantiationAwareBeanPostProcessor, DestructionAwareBeanPostProcessor {

        @Override
        public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
            if (bean instanceof User) {
                System.out.println("postProcessBeforeDestruction...");
            }
        }

        @Override
        public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
            if (beanClass == User.class) {
                System.out.println("postProcessBeforeInstantiation...");
            }
            return InstantiationAwareBeanPostProcessor.super.postProcessBeforeInstantiation(beanClass, beanName);
        }

        @Override
        public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
            if (bean instanceof User) {
                System.out.println("postProcessAfterInstantiation...");
            }
            return InstantiationAwareBeanPostProcessor.super.postProcessAfterInstantiation(bean, beanName);
        }

        @Override
        public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
            if (bean instanceof User) {
                System.out.println("postProcessProperties...");
            }
            return InstantiationAwareBeanPostProcessor.super.postProcessProperties(pvs, bean, beanName);
        }

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof User) {
                System.out.println("postProcessBeforeInitialization...");
            }
            return InstantiationAwareBeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof User) {
                System.out.println("postProcessAfterInitialization...");
            }
            return InstantiationAwareBeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
        }
    }
}
