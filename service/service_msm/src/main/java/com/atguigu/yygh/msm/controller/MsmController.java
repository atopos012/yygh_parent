package com.atguigu.yygh.msm.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.msm.service.MsmService;
import com.atguigu.yygh.msm.utils.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/msm")
public class MsmController {
    @Autowired
    private MsmService msmService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @GetMapping(value = "/send/{phone}")
    public R code(@PathVariable String phone) {

        //在有效期内，验证码不能重复发送
        String code = redisTemplate.opsForValue().get(phone);
        if (!StringUtils.isEmpty(code)) return R.ok();
        //生成验证码
        code = RandomUtil.getFourBitRandom();
        //调用service方法发送
        boolean is_success = msmService.sendMsm(phone, code);
        //如果发送成功,将验证码放到redis中,设置有效时长
        if (is_success) {
            //key为手机号,value为验证码
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);
            return R.ok();
        } else {
            return R.error();
        }

    }
}
