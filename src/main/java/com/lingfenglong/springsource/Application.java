package com.lingfenglong.springsource;

import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@EnableAspectJAutoProxy
@SpringBootApplication
public class Application {

    @Bean
    public String username() {
        return "LingFenglong";
    }

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

        // /*
        //  * BeanFactory 是 ApplicationContext 的父接口
        //  * ApplicationContext 中组合了 BeanFactory 的实现类
        //  */
        // String username = context.getBean("username", String.class);
        //
        // context.getBeanFactory()
        //         .getBean("username", String.class);
        //
        // /*
        //  * 获取 DefaultSingletonBeanRegistry 类中的 singletonObjects
        //  */
        // BeanFactory beanFactory = context.getBeanFactory();
        // Field singletonObjects = DefaultSingletonBeanRegistry.class.getDeclaredField("singletonObjects");
        // singletonObjects.setAccessible(true);
        // Map<String, Object> map = (Map<String, Object>) singletonObjects.get(beanFactory);
        // map
        //         .entrySet()
        //         .stream()
        //         .filter(entry -> entry.getKey().equals("username"))
        //         .forEach(System.out::println);
        //
        //
        // /*
        // - 加载资源
        // */
        // Resource[] resources = context.getResources("classpath*:META-INF/spring/*.imports");
        // Arrays.stream(resources)
        //         .forEach(System.out::println);
        //
        // /*
        // - 事件发布
        // */
        // context.publishEvent(new UserRegisteredEvent(new User(1, "LingFenglong")));
        //
        // /*
        // - 国际化支持
        // */
        // String ch = context.getMessage("hi", new Object[]{"LingFenglong"}, Locale.CHINA);
        // String en = context.getMessage("hi", new Object[]{"LingFenglong"}, Locale.ENGLISH);
        // // String ch = context.getMessage("hi", null, Locale.CHINA);
        // // String en = context.getMessage("hi", null, Locale.ENGLISH);
        //
        // System.out.println("ch = " + ch);
        // System.out.println("en = " + en);
        //
        // /*
        // - 可继承的上下文
        // */
        //
        // /*
        // - 获取环境变量
        //  */
        //
        // String property = context.getEnvironment().getProperty("spring.messages.basename");
        // System.out.println("property = " + property);
    }

    @Order(1)
    @Bean
    ApplicationRunner sendEvent(UserRegister userRegister, ApplicationEventPublisher applicationEventPublisher) {
        return args -> {
            User lingFenglong = new User(1, "LingFenglong");
            userRegister.register(lingFenglong);
            applicationEventPublisher.publishEvent(new UserRegisteredEvent(lingFenglong));
        };
    }

    @Order(2)
    @Bean
    ApplicationRunner aop(UserRegister userRegister) {
        return args -> {
            User zhangSan = new User(2, "ZhangSan");
            userRegister.register(zhangSan);
        };
    }
}

@Aspect
@Component
class UserRegisterAspect {
    private final SendMessage sendMessage;

    public UserRegisterAspect(SendMessage sendMessage) {
        this.sendMessage = sendMessage;
    }

    @AfterReturning(value = "execution(public User com.lingfenglong.springsource.UserRegister.register(User)))", returning = "user")
    public void userRegisterAdvice(User user) {
        sendMessage.sendMessage(user);
    }
}

class UserRegisteredEvent extends ApplicationEvent {

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredEvent.class);

    public UserRegisteredEvent(User user) {
        super(user);
        log.info("UserRegisteredEvent {}", user);
    }
}

@Component
class SendMessage {

    private static final Logger log = LoggerFactory.getLogger(SendMessage.class);

    @EventListener
    public void sendMessage(UserRegisteredEvent event) {
        log.info("SendMessage to {}", event.getSource());
    }

    public void sendMessage(User user) {
        log.info("SendMessage to {}", user);
    }
}

record User(Integer id, String name) {

}

@Component
class UserRegister {
    private static final Logger log = LoggerFactory.getLogger(UserRegister.class);

    public User register(User user) {
       log.info("register user {}", user);
       return user;
    }
}
