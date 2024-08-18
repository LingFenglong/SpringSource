package com.lingfenglong.springsource.scope;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

public class ApplicationTest {
    /*
    singleton
    prototype
    request
    session
    application
     */
    @Test
    public void scopeTest() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                BeanContainer.class, PrototypeBean1.class, PrototypeBean2.class, PrototypeBean3.class, PrototypeBean4.class
        );

        BeanContainer beanContainer = context.getBean(BeanContainer.class);
        System.out.println(beanContainer.getPrototypeBean1());
        System.out.println(beanContainer.getPrototypeBean1());
        System.out.println(beanContainer.getPrototypeBean1());
        System.out.println(beanContainer.getPrototypeBean1());
        System.out.println(beanContainer.getPrototypeBean1());

        System.out.println(beanContainer.getPrototypeBean2());
        System.out.println(beanContainer.getPrototypeBean2());
        System.out.println(beanContainer.getPrototypeBean2());
        System.out.println(beanContainer.getPrototypeBean2());
        System.out.println(beanContainer.getPrototypeBean2());

        System.out.println(beanContainer.getPrototypeBean3());
        System.out.println(beanContainer.getPrototypeBean3());
        System.out.println(beanContainer.getPrototypeBean3());
        System.out.println(beanContainer.getPrototypeBean3());
        System.out.println(beanContainer.getPrototypeBean3());

        System.out.println(beanContainer.getPrototypeBean4());
        System.out.println(beanContainer.getPrototypeBean4());
        System.out.println(beanContainer.getPrototypeBean4());
        System.out.println(beanContainer.getPrototypeBean4());
        System.out.println(beanContainer.getPrototypeBean4());

        context.close();
    }
}

@Component
class BeanContainer {
    @Autowired
    @Lazy
    private PrototypeBean1 prototypeBean1;

    @Autowired
    private PrototypeBean2 prototypeBean2;

    @Autowired
    private ObjectFactory<PrototypeBean3> prototypeBean3;

    @Autowired
    private ApplicationContext applicationContext;

    public PrototypeBean1 getPrototypeBean1() {
        return prototypeBean1;
    }

    public PrototypeBean2 getPrototypeBean2() {
        return prototypeBean2;
    }

    public PrototypeBean3 getPrototypeBean3() {
        return prototypeBean3.getObject();
    }

    public PrototypeBean4 getPrototypeBean4() {
        return applicationContext.getBean(PrototypeBean4.class);
    }
}

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
class TestController {
    private final Bean1 bean1;
    private final Bean2 bean2;
    private final Bean3 bean3;

    @Lazy
    public TestController(Bean1 bean1, Bean2 bean2, Bean3 bean3) {
        this.bean1 = bean1;
        this.bean2 = bean2;
        this.bean3 = bean3;
    }

    @GetMapping("/test")
    public String test() {
        return bean1.toString() + "<br>" + bean2.toString() + "<br>" + bean3.toString();
    }
}

@Scope("request")
@Component
class Bean1 {}

@Scope("session")
@Component
class Bean2 {}

@Scope("application")
@Component
class Bean3 {}

@Scope("prototype")
@Component
class PrototypeBean1 {}

@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component
class PrototypeBean2 {}

@Scope("prototype")
@Component
class PrototypeBean3 {}

@Scope("prototype")
@Component
class PrototypeBean4 {}

