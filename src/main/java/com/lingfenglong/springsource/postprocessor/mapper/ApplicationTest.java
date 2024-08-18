package com.lingfenglong.springsource.postprocessor.mapper;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.AnnotationUtils;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.*;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.lang.annotation.*;
import java.util.Optional;

public class ApplicationTest {
    @Test
    public void mapperTest() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Config.class);

        Assertions.assertNotNull(context.getBean(Config.class));
        Assertions.assertNotNull(context.getBean(SqlSessionFactory.class));
        Assertions.assertNotNull(context.getBean(Mapper1.class));
        Assertions.assertNotNull(context.getBean(Mapper2.class));
        Assertions.assertNotNull(context.getBean(Mapper3.class));
    }

    @Test
    public void myMapperScanTest() {
        GenericApplicationContext context = new GenericApplicationContext();

        context.registerBean(ConfigurationClassPostProcessor.class);
        context.registerBean(MyMapperScanPostProcessor.class);
        context.registerBean(Config.class);

        context.refresh();
        for (String name : context.getBeanDefinitionNames()) {
            System.out.println(name);
        }
        context.close();
    }

    @Test
    public void beanDefinitionTest() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("message", String.class, () -> "JAVA_JAVA");

        AnnotationConfigUtils.registerAnnotationConfigProcessors(context);

        BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(Bean1FactoryBean.class)
                .setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE)
                .getBeanDefinition();
        context.registerBeanDefinition("bean1FactoryBean", beanDefinition);

        context.refresh();
        String[] names = context.getBeanDefinitionNames();
        for (String name : names) {
            System.out.println(name);
        }
        System.out.println(context.getBean(Bean1.class));
        context.close();
    }
}

class MyMapperScanPostProcessor implements BeanFactoryPostProcessor {
    private final Class<Config> config = Config.class;
    private final PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    private final CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();
    private final FullyQualifiedAnnotationBeanNameGenerator beanNameGenerator = new FullyQualifiedAnnotationBeanNameGenerator();

    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        Optional<Configuration> configuration = AnnotationUtils.findAnnotation(config, Configuration.class);
        if (configuration.isEmpty()) {
            return;
        }

        Optional<MyMapperScan> mapperScan = AnnotationUtils.findAnnotation(config, MyMapperScan.class);
        if (mapperScan.isEmpty()) {
            return;
        }

        String[] basePackages = mapperScan.get().basePackages();
        try {
            for (String basePackage : basePackages) {
                String path = "classpath*:" + basePackage.replaceAll("\\.", "/") + "/**/*.class";
                Resource[] resources = resourcePatternResolver.getResources(path);

                for (Resource resource : resources) {
                    MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                    System.out.println("类名：" + metadataReader.getClassMetadata().getClassName());
                    // 不是接口
                    if (!metadataReader.getClassMetadata().isInterface()) {
                        continue;
                    }
                    // 没有Mapper标注
                    if (!hasMapperAnnotation(metadataReader)) {
                        continue;
                    }

                    if (configurableListableBeanFactory instanceof DefaultListableBeanFactory beanFactory) {
                        BeanDefinition beanDefinition = BeanDefinitionBuilder
                                .genericBeanDefinition(MapperFactoryBean.class)
                                .addPropertyValue("mapperInterface", Class.forName(metadataReader.getClassMetadata().getClassName()))
                                .setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE)
                                .getBeanDefinition();


                        BeanDefinition mapperNameBeanDefinition = BeanDefinitionBuilder
                                .genericBeanDefinition(metadataReader.getClassMetadata().getClassName())
                                .getBeanDefinition();
                        String beanName = beanNameGenerator.generateBeanName(mapperNameBeanDefinition, beanFactory);

                        beanFactory.registerBeanDefinition(beanName, beanDefinition);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean hasMapperAnnotation(MetadataReader metadataReader) {
        return metadataReader.getAnnotationMetadata().hasAnnotation(Mapper.class.getName())
                || metadataReader.getAnnotationMetadata().hasMetaAnnotation(Mapper.class.getName());
    }
}

@MyMapperScan(basePackages = "com.lingfenglong.springsource.postprocessor.mapper")
@Configuration
class Config {
    @Bean
    public SqlSessionFactoryBean sqlSessionFactoryBean() {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(new HikariDataSource());
        return sqlSessionFactoryBean;
    }

    // @Bean
    // public MapperFactoryBean<Mapper1> mapper1(SqlSessionFactory sqlSessionFactory) {
    //     MapperFactoryBean<Mapper1> factoryBean = new MapperFactoryBean<>();
    //     factoryBean.setMapperInterface(Mapper1.class);
    //     factoryBean.setSqlSessionFactory(sqlSessionFactory);
    //     return factoryBean;
    // }

    // @Bean
    // public MapperFactoryBean<Mapper2> mapper2(SqlSessionFactory sqlSessionFactory) {
    //     MapperFactoryBean<Mapper2> factoryBean = new MapperFactoryBean<>();
    //     factoryBean.setMapperInterface(Mapper2.class);
    //     factoryBean.setSqlSessionFactory(sqlSessionFactory);
    //     return factoryBean;
    // }
    //
    // @Bean
    // public MapperFactoryBean<Mapper3> mapper3(SqlSessionFactory sqlSessionFactory) {
    //     MapperFactoryBean<Mapper3> factoryBean = new MapperFactoryBean<>();
    //     factoryBean.setMapperInterface(Mapper3.class);
    //     factoryBean.setSqlSessionFactory(sqlSessionFactory);
    //     return factoryBean;
    // }
}

@Mapper
interface Mapper1 {

}

@Mapper
interface Mapper2 {

}

interface Mapper3 {

}



class Bean1 implements InitializingBean {
    private String message;
    private String a;

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMapper1(Mapper1 mapper1) {
        System.out.println("setMapper1");
    }

    public void setA(String a) {
        System.out.println("setA");
    }

    public void setB() {
        System.out.println("setB");
    }

    public void setC() {
        System.out.println("setC");
    }

    public void setD() {
        System.out.println("setD");
    }

    @Override
    public String toString() {
        return "Bean1{" +
                "message='" + message + '\'' +
                ", a='" + a + '\'' +
                '}';
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}

class Bean1FactoryBean implements FactoryBean<Bean1> {
    private String message;

    public String getMessage() {
        System.out.println("getMessage");
        return message;
    }

    public void setMessage(String message) {
        System.out.println("setMessage");
        this.message = message;
    }

    @Override
    public Bean1 getObject() throws Exception {
        Bean1 bean1 = new Bean1();
        bean1.setMessage(message);
        return bean1;
    }

    @Override
    public Class<?> getObjectType() {
        return Bean1.class;
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface MyMapperScan {
    String[] basePackages() default "com.lingfenglong.springsource.postprocessor.mapper";
}