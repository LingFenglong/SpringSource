package com.lingfenglong.springsource.postprocessor.autowire;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.annotations.Mapper;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.*;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ApplicationTest {
    @Test
    public void autowiredAnnotationBeanPostProcessorTest() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        beanFactory.registerSingleton("bean2", new Bean2());
        beanFactory.registerSingleton("bean3", new Bean3());
        beanFactory.registerSingleton("bean4", new Bean4());

        beanFactory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());
        beanFactory.addEmbeddedValueResolver(new StandardEnvironment()::resolvePlaceholders);

        AutowiredAnnotationBeanPostProcessor postProcessor = new AutowiredAnnotationBeanPostProcessor();
        postProcessor.setBeanFactory(beanFactory);
        Bean1 bean1 = new Bean1();
        System.out.println("bean1 = " + bean1);
        postProcessor.postProcessProperties(null, bean1, "bean1");
        System.out.println("bean1 = " + bean1);
    }

    @Test
    public void autowireTest() throws NoSuchFieldException, NoSuchMethodException {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        beanFactory.registerSingleton("bean2", new Bean2());
        beanFactory.registerSingleton("bean3", new Bean3());
        beanFactory.registerSingleton("bean4", new Bean4());

        beanFactory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());
        beanFactory.addEmbeddedValueResolver(new StandardEnvironment()::resolvePlaceholders);

        // 1
        Field bean2 = Bean1.class.getDeclaredField("bean2");
        DependencyDescriptor dd1 = new DependencyDescriptor(bean2, true);
        Object o1 = beanFactory.resolveDependency(dd1, null, null, null);
        System.out.println(o1);

        // 2
        Field bean3 = Bean1.class.getDeclaredField("bean3");
        DependencyDescriptor dd2 = new DependencyDescriptor(bean3, true);
        Object o2 = beanFactory.resolveDependency(dd2, null, null, null);
        System.out.println(o2);

        // 3
        Method setJavaHome = Bean1.class.getDeclaredMethod("setJavaHome", String.class);
        DependencyDescriptor dd3 = new DependencyDescriptor(new MethodParameter(setJavaHome, 0), true);
        Object o3 = beanFactory.resolveDependency(dd3, null, null, null);
        System.out.println(o3);
    }

    @Test
    void configurationClassPostProcessorTest() {
        GenericApplicationContext context = new GenericApplicationContext();

        context.registerBean("config", Config.class);

        // @ComponentScan @Bean @Import @ImportResource
        context.registerBean(ConfigurationClassPostProcessor.class);

        context.refresh();
        for (String name : context.getBeanDefinitionNames()) {
            System.out.println(name);
        }
        context.close();
    }

    @Test
    void mapperPostProcessTest() {
        GenericApplicationContext context = new GenericApplicationContext();

        // @MapperScan
        context.registerBean(ConfigurationClassPostProcessor.class);

        context.registerBean(MapperScannerConfigurer.class, bd -> {
            bd.getPropertyValues()
                    .add("basePackage", "com.lingfenglong.springsource.postprocessor.autowire");
        });

        context.registerBean(SqlSessionFactoryBean.class, () -> {
            SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
            sqlSessionFactoryBean.setDataSource(new HikariDataSource());
            return sqlSessionFactoryBean;
        });

        context.refresh();
        for (String name : context.getBeanDefinitionNames()) {
            System.out.println(name);
        }
        context.close();
    }
}

@Configuration
class Config {
    @Bean
    public Bean2 bean2() {
        return new Bean2();
    }
}

@Component
class Bean1 {
    @Autowired
    private Bean2 bean2;

    private Bean3 bean3;

    private Bean4 bean4;

    private String javaHome;

    @Autowired
    public void setBean3(Bean3 bean3) {
        this.bean3 = bean3;
    }

    @Autowired
    public Bean1(Bean4 bean4) {
        this.bean4 = bean4;
    }

    public Bean1() {
    }

    @Autowired
    public void setJavaHome(@Value("${JAVA_HOME}") String javaHome) {
        this.javaHome = javaHome;
    }

    @Override
    public String toString() {
        return "Bean1{" +
                "bean2=" + bean2 +
                ", bean3=" + bean3 +
                ", bean4=" + bean4 +
                ", javaHome='" + javaHome + '\'' +
                '}';
    }
}

@Mapper
interface Bean2Mapper {

}

@Component
record Bean2() {

}

@Component
record Bean3() {

}

@Component
record Bean4() {

}


