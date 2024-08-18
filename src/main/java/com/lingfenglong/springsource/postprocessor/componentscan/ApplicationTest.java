package com.lingfenglong.springsource.postprocessor.componentscan;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.AnnotationUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.Optional;

public class ApplicationTest {

    @Test
    public void myComponentScanPostProcessor() {
        GenericApplicationContext context = new GenericApplicationContext();

        context.registerBean(MyComponentScanPostProcessor.class);

        context.refresh();
        for (String name : context.getBeanDefinitionNames()) {
            System.out.println(name);
        }
        context.close();
    }
}

class MyComponentScanPostProcessor implements BeanFactoryPostProcessor {

    private final BeanNameGenerator beanNameGenerator = new FullyQualifiedAnnotationBeanNameGenerator();
    private final SimpleMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        Optional<ComponentScan> componentScan = AnnotationUtils.findAnnotation(Config.class, ComponentScan.class);
        if (componentScan.isEmpty()) {
            return;
        }

        String[] basePackages = componentScan.get().basePackages();
        for (String basePackage : basePackages) {
            String path = "classpath*:" + basePackage.replaceAll("\\.", "/") + "/**/*.class";

            try {
                Resource[] resources = new PathMatchingResourcePatternResolver().getResources(path);
                for (Resource resource : resources) {
                    MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);

                    System.out.println("类名：" + metadataReader.getClassMetadata().getClassName());
                    System.out.println("是否存在@Component：" + hasComponentAnnotation(metadataReader));

                    if (!hasComponentAnnotation(metadataReader)) {
                        continue;
                    }

                    if (configurableListableBeanFactory instanceof DefaultListableBeanFactory beanFactory) {
                        BeanDefinition beanDefinition = BeanDefinitionBuilder
                                .genericBeanDefinition(metadataReader.getClassMetadata().getClassName())
                                .getBeanDefinition();
                        String beanName = beanNameGenerator.generateBeanName(beanDefinition, beanFactory);
                        beanFactory.registerBeanDefinition(beanName, beanDefinition);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean hasComponentAnnotation(MetadataReader metadataReader) {
        return metadataReader.getAnnotationMetadata().hasMetaAnnotation(Component.class.getName())
                || metadataReader.getAnnotationMetadata().hasAnnotation(Component.class.getName());
    }

}

@Configuration
@ComponentScan(basePackages = "com.lingfenglong.springsource.postprocessor.componentscan")
class Config {

}

@Component
record Bean1() {

}

@Controller
record Bean2() {

}

record Bean3() {

}
