package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DepartmentService {
    void save(Map<String, Object> newObjectMap);

    Page<Department> selectPageDept(int page, int limit, String hoscode, String depcode);

    void remove(String hoscode, String depcode);

    List<DepartmentVo> find(String hospCode);
}
