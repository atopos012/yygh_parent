package com.atguigu.yygh.hosp.controller;


import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.hosp.utils.HttpRequestHelper;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.hosp.HospitalSetQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * <p>
 * 医院设置表 前端控制器
 * </p>
 *
 * @author ATOPOS
 * @since 2022-10-17
 */
//@CrossOrigin //跨域
@Api(tags = "医院设置管理")
@RestController
@RequestMapping("/admin/hosp/hospitalSet/")
public class HospitalSetController {
    @Autowired
    private HospitalSetService hospitalSetService;

    //查询所有医院设置
    @ApiOperation(value = "医院设置列表")
    @GetMapping("findAll")
    public R findAll() {
        //模拟异常
        try {
            int i = 1 / 0;
        } catch (Exception e) {
            throw new YyghException(20001, "执行自定义异常处理");
        }
        List<HospitalSet> list = hospitalSetService.list();
        return R.ok().data("list", list);
    }

    @ApiOperation(value = "医院设置删除")
    @DeleteMapping("remove/{id}")
    public R removeById(@ApiParam(name = "id", value = "ID", required = true) @PathVariable String id) {
        boolean is_success = hospitalSetService.removeById(id);
        if (is_success) {
            return R.ok();
        } else {
            return R.error();
        }
    }

    @ApiOperation("分页查询")
    @GetMapping("findPage/{current}/{limit}")
    public R findPage(@PathVariable Long current,
                      @PathVariable Long limit) {
        //创建page对象，传递current,page
        Page<HospitalSet> pageParam = new Page<>(current, limit);
        //调用方法实现分页查询
        hospitalSetService.page(pageParam);//封装分页查询数据
        List<HospitalSet> list = pageParam.getRecords();
        long total = pageParam.getTotal();
        //返回数据,方式一
        //return R.ok().data("total",total).data("list",list);

        //返回数据，方式二
        Map<String, Object> map = new HashMap<>();
        map.put("total", total);
        map.put("list", list);
        return R.ok().data(map);
    }

    @ApiOperation("条件分页查询")
    @GetMapping("findPageQuery/{current}/{limit}")
    public R findPageQuery(@PathVariable Long current,
                           @PathVariable Long limit,
                           @PathVariable HospitalSetQueryVo queryVo) {
        //创建page对象，传递current,page
        Page<HospitalSet> pageParam = new Page<>(current, limit);
        //封装条件
        if (queryVo == null) {//条件对象为空，查询全部
            //调用方法实现分页查询
            hospitalSetService.page(pageParam);//封装分页查询数据
        } else {//条件对象不为空，执行条件查询
            QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
            //判断条件值是否为空
            String hoscode = queryVo.getHoscode();
            String hosname = queryVo.getHosname();
            if (!StringUtils.isEmpty(hoscode)) {
                wrapper.eq("hoscode", hoscode);
            }
            if (!StringUtils.isEmpty(hosname)) {
                wrapper.like("hosname", hosname);
            }
            //调用方法，条件分页查询
            hospitalSetService.page(pageParam, wrapper);
        }
        List<HospitalSet> list = pageParam.getRecords();
        long total = pageParam.getTotal();
        //返回数据
        return R.ok().data("total", total).data("list", list);
    }

    @ApiOperation("条件分页查询RequestBody")
    @PostMapping("findPageQueryHosp/{current}/{limit}")
    public R findPageQueryHosp(@PathVariable Long current,
                               @PathVariable Long limit,
                               @RequestBody(required = false) HospitalSetQueryVo queryVo) {
        //创建page对象，传递current,page
        Page<HospitalSet> pageParam = new Page<>(current, limit);
        //封装条件
        if (queryVo == null) {//条件对象为空，查询全部
            //调用方法实现分页查询
            hospitalSetService.page(pageParam);//封装分页查询数据
        } else {//条件对象不为空，执行条件查询
            QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
            //判断条件值是否为空
            String hoscode = queryVo.getHoscode();
            String hosname = queryVo.getHosname();
            if (!StringUtils.isEmpty(hoscode)) {
                wrapper.like("hoscode", hoscode);
            }
            if (!StringUtils.isEmpty(hosname)) {
                wrapper.like("hosname", hosname);
            }
            String updateTime = queryVo.getUpdateTime();
            wrapper.orderByDesc("update_time", updateTime);
            //调用方法，条件分页查询
            hospitalSetService.page(pageParam, wrapper);
        }
        List<HospitalSet> list = pageParam.getRecords();
        long total = pageParam.getTotal();
        //返回数据
        return R.ok().data("total", total).data("list", list);
    }

    @ApiOperation("添加医院（平台中注册医院信息）")
    @PostMapping("saveHospSet")
    public R saveHospSet(@RequestBody HospitalSet hospitalSet) {
        //为每个医院生成唯一的字符串，签名密钥
        String singKey = System.currentTimeMillis() + "" + new Random().nextInt(1000);
        //设置到hospitalSet中
        hospitalSet.setSignKey(singKey);
        boolean is_success = hospitalSetService.save(hospitalSet);//将医院信息存储到MySQL中
        if (is_success) {//平台中保留成功
            //将为这个医院生成的唯一的字符串保存到医院系统中
            //平台调用医院模拟系统中同步接口
            //同步医院系统签名
            Map<String, Object> map = new HashMap<>();
            map.put("sign", singKey);//签名密钥
            map.put("hoscode", hospitalSet.getHoscode());
            //使用httpClint调用
            HttpRequestHelper.sendRequest(map, "http://localhost:9998/hospSet/updateSignKey");
            return R.ok();
        } else {
            return R.error();
        }
    }

    @ApiOperation("根据id查询")
    @GetMapping("getHospSet/{id}")
    public R getHospSet(@PathVariable Long id) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        return R.ok().data("hospitalSet", hospitalSet);
    }

    @ApiOperation("根据id修改")
    @PutMapping("updateHospSet")
    public R updateHospSet(@RequestBody HospitalSet hospitalSet) {
        boolean is_success = hospitalSetService.updateById(hospitalSet);
        if (is_success) {
            return R.ok();
        } else {
            return R.error();
        }
    }

    /**
     * json对象格式：{...}   -- java对象
     * json数组格式：[1,2,3] --java的List集合
     *
     * @param idList 需要删除id的列表
     * @return
     */
    @ApiOperation("批量删除")
    @DeleteMapping("deleteBatch")
    public R deleteBatch(@RequestBody List<Long> idList) {
        //使用@RequestBody 获取json数组格式，数组有多个id值
        hospitalSetService.removeByIds(idList);
        return R.ok();
    }

    @ApiOperation("医院锁定及解锁")
    @PutMapping("lockHospitalSet/{id}/{status}")
    public R lockHospitalSet(@PathVariable Long id,
                             @PathVariable Integer status) {
        //根据id查询医院设置信息
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        //设置状态
        hospitalSet.setStatus(status);
        //调用方法
        hospitalSetService.updateById(hospitalSet);
        return R.ok();
    }

}

