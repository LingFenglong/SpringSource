package com.lingfenglong.springsource.postprocessor.use;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

public class ApplicationTest {


    @Test
    public void postProcessor() {
        GenericApplicationContext context = new GenericApplicationContext();

        context.registerBean("bean1", Bean1.class);
        context.registerBean("bean2", Bean2.class);
        context.registerBean("bean3", Bean3.class);
        context.registerBean("bean4", Bean4.class);

        DefaultListableBeanFactory defaultListableBeanFactory = context.getDefaultListableBeanFactory();

        // @Value("${JAVA_HOME}")
        defaultListableBeanFactory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());

        // @Autowired @Value
        context.registerBean(AutowiredAnnotationBeanPostProcessor.class);

        // @Resource @PreDestroy @PostConstruct
        context.registerBean(CommonAnnotationBeanPostProcessor.class);

        // @ConfigurationProperties
        ConfigurationPropertiesBindingPostProcessor.register(defaultListableBeanFactory);

        context.refresh();

        Bean4 bean4 = context.getBean(Bean4.class);
        System.out.println("context.getEnvironment().getProperty(\"java.home\") = " + context.getEnvironment().getProperty("java.home"));
        System.out.println("context.getEnvironment().getProperty(\"java.version\") = " + context.getEnvironment().getProperty("java.version"));
        System.out.println("bean4 = " + bean4);

        context.close();
    }
}

@Component
class Bean1 {
    private Bean2 bean2;
    private Bean3 bean3;
    private String home;

    @Autowired
    public void setBean2(Bean2 bean2) {
        System.out.println("Autowired");
        this.bean2 = bean2;
    }

    @Resource
    public void setBean3(Bean3 bean3) {
        System.out.println("Resource");
        this.bean3 = bean3;
    }

    @Autowired
    public void setHome(@Value("${JAVA_HOME}") String home) {
        System.out.println("Value");
        this.home = home;
    }

    @PostConstruct
    public void init() {
        System.out.println("PostConstruct");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("PreDestroy");
    }
}

@Component
record Bean2() {

}

@Component
record Bean3() {

}

@Component
@ConfigurationProperties(prefix = "java")
class Bean4 {
    private String home;
    private String version;

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        System.out.println("ConfigurationProperties");
        return "Bean4{" +
                "home='" + home + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
