package com.lingfenglong.springsource.bean;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletRegistrationBean;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.Controller;

import java.io.PrintWriter;
import java.util.Arrays;

public class SpringSourceApplication {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
        User user = context.getBean(User.class);
        System.out.println(user);

        Arrays.stream(context.getBeanDefinitionNames())
                .forEach(System.out::println);
    }

    @Test
    public void xmlBeanDefinitionReader() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
        xmlBeanDefinitionReader.loadBeanDefinitions("beans.xml");

        User user = beanFactory.getBean("user", User.class);
        System.out.println("user = " + user);
    }

    @Test
    void annotationConfig() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(UserConfig.class);
        User user = context.getBean(User.class);
        System.out.println("user = " + user);

        Arrays.stream(context.getBeanDefinitionNames())
                .forEach(System.out::println);
    }

    @Test
    void annotationBeanDefinitionReader() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        AnnotatedBeanDefinitionReader beanDefinitionReader = new AnnotatedBeanDefinitionReader(beanFactory);
        beanDefinitionReader.register(UserConfig.class);

        // 添加处理器
        AnnotationConfigUtils.registerAnnotationConfigProcessors(beanFactory);
        beanFactory.getBeansOfType(BeanFactoryPostProcessor.class)
                .values()
                .forEach(beanFactoryPostProcessor -> beanFactoryPostProcessor.postProcessBeanFactory(beanFactory));
        beanFactory.getBeansOfType(BeanPostProcessor.class)
                .values()
                .forEach(beanFactory::addBeanPostProcessor);

        User user = beanFactory.getBean(User.class);
        System.out.println("user = " + user);
    }

    @Test
    void webContext() {
        AnnotationConfigServletWebServerApplicationContext context =
                new AnnotationConfigServletWebServerApplicationContext(WebConfig.class);

        try { Thread.sleep(1000000); } catch (InterruptedException e) { throw new RuntimeException(e); }
    }
}

@Configuration
class WebConfig implements WebMvcConfigurer {
    @Bean
    public TomcatServletWebServerFactory tomcatServletWebServerFactory() {
        return new TomcatServletWebServerFactory();
    }

    @Bean
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet();
    }

    @Bean
    public DispatcherServletRegistrationBean dispatcherServletRegistrationBean(DispatcherServlet dispatcherServlet) {
        return new DispatcherServletRegistrationBean(dispatcherServlet, "/");
    }

    @Bean("/hello")
    public Controller helloController() {
        return (request, response) -> {
            PrintWriter writer = response.getWriter();
            writer.write("Hello!");
            writer.flush();
            writer.close();
            return null;
        };
    }
}

@Configuration
class UserConfig {
    @Bean
    public User user() {
        return new User(1, "ZhangSan");
    }
}

record User(Integer id, String name) {
}