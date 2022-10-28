package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.hosp.utils.HttpRequestHelper;
import com.atguigu.yygh.hosp.utils.MD5;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/hosp/")
public class ApiController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private HospitalSetService hospitalSetService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;

    //删除排班信息
    @PostMapping("schedule/remove")
    public Result removeSchedule(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //必须参数校验 略
        String hoscode = (String) paramMap.get("hoscode");
        String hosScheduleId = (String) paramMap.get("hosScheduleId");
        scheduleService.remove(hoscode, hosScheduleId);
        return Result.ok();
    }

    //获取排班信息分页列表
    @PostMapping("schedule/list")
    public Result schedule(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //必须参数校验 略
        String hoscode = (String) paramMap.get("hoscode");
        //非必填
        String depcode = (String) paramMap.get("depcode");
        int page = StringUtils.isEmpty(paramMap.get("page")) ? 1 : Integer.parseInt((String) paramMap.get("page"));
        int limit = StringUtils.isEmpty(paramMap.get("limit")) ? 10 : Integer.parseInt((String) paramMap.get("limit"));
        Page<Schedule> pageModel = scheduleService.selectPageSchedule(page, limit, hoscode, depcode);

        return Result.ok(pageModel);
    }

    //上传排班信息
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request) {

        Map<String, Object> newObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        scheduleService.save(newObjectMap);
        return Result.ok();
    }

    //删除科室
    @PostMapping("department/remove")
    public Result removeDepartment(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //必须参数校验 略
        String hoscode = (String) paramMap.get("hoscode");
        String depcode = (String) paramMap.get("depcode");
        departmentService.remove(hoscode, depcode);
        return Result.ok();
    }

    //获取科室分页列表
    @PostMapping("department/list")
    public Result department(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //必须参数校验 略
        String hoscode = (String) paramMap.get("hoscode");
        //非必填
        String depcode = (String) paramMap.get("depcode");
        int page = StringUtils.isEmpty(paramMap.get("page")) ? 1 : Integer.parseInt((String) paramMap.get("page"));
        int limit = StringUtils.isEmpty(paramMap.get("limit")) ? 10 : Integer.parseInt((String) paramMap.get("limit"));
        Page<Department> pageModel = departmentService.selectPageDept(page, limit, hoscode, depcode);

        return Result.ok(pageModel);
    }

    //上传科室
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request) {

        Map<String, Object> newObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        departmentService.save(newObjectMap);
        return Result.ok();
    }

    //获取医院信息
    @PostMapping("hospital/show")
    public Result hospital(HttpServletRequest request) {

        Map<String, Object> newObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode = (String) newObjectMap.get("hoscode");
        Hospital hospital = hospitalService.getHosp(hoscode);

        return Result.ok(hospital);
    }

    //上传医院设置
    @PostMapping("saveHospital")
    public Result saveHospital(HttpServletRequest request) {
        //获取提交参数，封装到map集合中
        Map<String, String[]> parameterMap = request.getParameterMap();
        //为了方便操作 Map<String, String[]>  ---> Map<String, Object>
        Map<String, Object> newObjectMap = HttpRequestHelper.switchMap(parameterMap);

        //添加签名校验
        //获取医院模拟系统传递的sign,MD5加密
        String signHospital = (String) newObjectMap.get("sign");
        //查询当前医院在平台保存的sign
        String hoscode = (String) newObjectMap.get("hoscode");
        String signYygh = hospitalSetService.getHospSignKey(hoscode);
        //比对两个sign
        String md5SignYygh = MD5.encrypt(signYygh);
        if (!signHospital.equals(md5SignYygh)) {
            throw new YyghException(20001, "签名校验失败");
        }

        //传输过程中“+”转换为了“ ”，因此我们要转换回来
        String logoData = (String) newObjectMap.get("logoData");
        logoData = logoData.replaceAll(" ", "+");
        newObjectMap.put("logoData", logoData);

        //调用service方法实现添加到MongoDB中
        hospitalService.saveHosp(newObjectMap);
        return Result.ok();
    }
}
