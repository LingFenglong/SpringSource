package com.lingfenglong.springsource.mybatis;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ApplicationTest {

    @Test
    void findById() throws IOException {
        SqlSession sqlSession = new SqlSessionFactoryBuilder()
                .build(Resources.getResourceAsStream("mybatis-config.xml"))
                .openSession();

        CustomerMapper mapper = sqlSession.getMapper(CustomerMapper.class);
        Customer customer = mapper.findById(1);
        System.out.println("customer = " + customer);

        sqlSession.clearCache();
        sqlSession.close();
    }
}

interface CustomerMapper {
    Customer findById(@Param("id") Integer id);
}

record Customer(Integer id, String name) {

}