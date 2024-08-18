package com.lingfenglong.springsource.aware;

import org.springframework.beans.BeansException;
import org.springframework.context.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;

public class AwareApplication {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AwareConfiguration.class);
        User user = context.getBean(User.class);
        System.out.println("user = " + user);
    }
}

class User implements ApplicationContextAware, ApplicationEventPublisherAware, ResourceLoaderAware, MessageSourceAware {
    private final Integer id;
    private final String name;
    private ApplicationContext applicationContext;
    private ApplicationEventPublisher applicationEventPublisher;
    private MessageSource messageSource;
    private ResourceLoader resourceLoader;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public User(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", applicationContext=" + applicationContext +
                ", applicationEventPublisher=" + applicationEventPublisher +
                ", messageSource=" + messageSource +
                ", resourceLoader=" + resourceLoader +
                '}';
    }
}

class AwareConfiguration {
    @Bean
    public User user() {
        return new User(1, "zs");
    }
}