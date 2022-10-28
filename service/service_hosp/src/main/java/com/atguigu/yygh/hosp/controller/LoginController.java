package com.atguigu.yygh.hosp.controller;


import com.atguigu.yygh.common.result.R;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 医院设置表 前端控制器
 * </p>
 *
 * @author ATOPOS
 * @since 2022-10-17
 */
//@CrossOrigin //跨域，添加GateWay网关之后会冲突
@RestController
@RequestMapping("/user/hosp")
public class LoginController {

    //login
    @PostMapping("login")
    public R login(){
        return R.ok().data("token","admin-token");
    }

    //info
    @GetMapping("info")
    public R info(){
        return R.ok().data("roles","admin")
                .data("introduction","I am a super administrator")
                .data("avatar","https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif")
                .data("name","Super Admin");
    }
}

