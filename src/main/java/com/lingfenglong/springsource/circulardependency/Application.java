package com.lingfenglong.springsource.circulardependency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class Application {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Application.class);
        application.setAllowCircularReferences(true);
        application.run(args);
    }

    @Bean
    ApplicationRunner applicationRunner(A a) {
        return args -> {
            System.out.println("a = " + a);
            System.out.println("a.getB() = " + a.getB());
        };
    }
}

@Component("A")
class A {
    private B b;

    public B getB() {
        return b;
    }

    @Autowired
    public void setB(B b) {
        this.b = b;
    }
}

@Component("B")
class B {
    private A a;

    public A getA() {
        return a;
    }

    @Autowired
    public void setA(A a) {
        this.a = a;
    }
}