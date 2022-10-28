package com.atguigu.yygh.hosp.code.mongo;


import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@SpringBootTest
public class TestMongoTemplate {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void add() {
        User user = new User();
        user.setAge(21);
        user.setName("jack");
        user.setEmail("jack@qq.com");

        //添加完成后会返回生成的主键id
        User insert = mongoTemplate.insert(user);
        System.out.println("insert = " + insert);
    }

    @Test
    public void findAll() {
        List<User> all = mongoTemplate.findAll(User.class);
        System.out.println("all = " + all);
    }

    @Test
    public void findQuery() {
        Query query = new Query(Criteria.where("name").is("test")
                .and("age").is(20));
        List<User> users = mongoTemplate.find(query, User.class);
    }

    @Test
    public void findLike() {
        String name = "j";
        String regex = String.format("%s%s%s", "^.*", name, ".*$");
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Query query = new Query(Criteria.where("name").regex(pattern));
        List<User> users = mongoTemplate.find(query, User.class);
        System.out.println("users = " + users);
    }


    @Test
    public void pageFind() {
        String name = "est";
        int pageNo = 1;
        int pageSize = 1;
        Query query = new Query();
        String regex = String.format("%s%s%s", ".*", name, ".*$");
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        query.addCriteria(Criteria.where("name").regex(pattern));
        int totalCount = (int) mongoTemplate.count(query, User.class);
        List<User> users = mongoTemplate.find(query.skip((pageNo - 1) * pageSize).limit(pageSize), User.class);
        Map<String,Object> pageMap = new HashMap<>();
        pageMap.put("list",users);
        pageMap.put("totalCount",totalCount);
        System.out.println("pageMap = " + pageMap);
    }
    
    @Test
    public void updateUser(){
        User user = mongoTemplate.findById("63560622194bf468de85b633", User.class);
        user.setName("test_1");
        user.setAge(25);
        user.setEmail("update@123.com");
        Query query = new Query(Criteria.where("_id").is(user.getId()));
        Update update = new Update();
        update.set("name",user.getName());
        update.set("age",user.getAge());
        update.set("email",user.getEmail());
        UpdateResult result = mongoTemplate.upsert(query,update,User.class);
        long modifiedCount = result.getModifiedCount();
        System.out.println("modifiedCount = " + modifiedCount);
    }

    @Test
    public void delete(){
        Query query = new Query(Criteria.where("_id").is("63560622194bf468de85b633"));
        DeleteResult result = mongoTemplate.remove(query, User.class);
        long deletedCount = result.getDeletedCount();
        System.out.println("deletedCount = " + deletedCount);
    }
}
