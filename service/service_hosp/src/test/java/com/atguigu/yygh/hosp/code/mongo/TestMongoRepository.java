package com.atguigu.yygh.hosp.code.mongo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;

import java.util.List;

@SpringBootTest
public class TestMongoRepository {

    @Autowired
    private UserRepository userRepository;
    
    @Test
    public void testQuery(){
        User user = new User();
        user.setName("zhangsan");
        user.setAge(21);
        Example<User> example =Example.of(user);
        List<User> all = userRepository.findAll(example);
        System.out.println("all = " + all);
    }

}