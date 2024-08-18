package com.lingfenglong.springsource.postprocessor.bean;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.AnnotationUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

public class ApplicationTest {
    @Test
    public void beanTest() {
        GenericApplicationContext context = new GenericApplicationContext();

        context.registerBean(MyBeanPostProcessor.class);
        context.registerBean("config", Config.class);

        context.refresh();
        for (String name : context.getBeanDefinitionNames()) {
            System.out.println(name);
        }
        context.close();
    }
}

class MyBeanPostProcessor implements BeanFactoryPostProcessor {
    private final CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();
    private final Class<Config> config = Config.class;

    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory configurableListableBeanFactory)
            throws BeansException {
        Optional<Configuration> configuration = AnnotationUtils.findAnnotation(config, Configuration.class);
        if (configuration.isEmpty()) {
            return;
        }

        try {
            MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(config.getName());
            Set<MethodMetadata> methods = metadataReader.getAnnotationMetadata().getAnnotatedMethods(Bean.class.getName());
            methods.forEach(method -> {
                System.out.println("Method name: " + method.getMethodName());
                if (configurableListableBeanFactory instanceof DefaultListableBeanFactory beanFactory) {
                    String initMethod = (String) method.getAnnotationAttributes(Bean.class.getName()).get("initMethod");

                    BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition()
                            .setFactoryMethodOnBean(method.getMethodName(), "config")
                            .setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR);
                    if (StringUtils.hasText(initMethod)) {
                        beanDefinitionBuilder.setInitMethodName(initMethod);
                    }
                    BeanDefinition beanDefinition = beanDefinitionBuilder
                            .getBeanDefinition();

                    beanFactory.registerBeanDefinition(method.getMethodName(), beanDefinition);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

@Configuration
class Config {
    @Bean
    public Bean1 bean1() {
        return new Bean1();
    }

    @Bean
    public Bean2 bean2(Bean1 bean1) {
        return new Bean2(bean1);
    }

    @Bean(initMethod = "init")
    public Bean3 bean3() {
        return new Bean3();
    }
}

record Bean1() {

}

record Bean2(Bean1 bean1) {

}

record Bean3() {
    public void init() {
        System.out.println("init...");
    }
}