package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.DepartmentRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public void save(Map<String, Object> newObjectMap) {
        String jsonString = JSONObject.toJSONString(newObjectMap);
        Department department = JSONObject.parseObject(jsonString, Department.class);
        Department existDepartment = departmentRepository.getDeptByHoscodeAndDepcode(department.getHoscode(), department.getDepcode());

        if (existDepartment != null) {
            //更新
            department.setId(existDepartment.getId());
            department.setCreateTime(existDepartment.getCreateTime());
            department.setUpdateTime(new Date());
            departmentRepository.save(department);
        } else {
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            departmentRepository.save(department);
        }
    }

    @Override
    public Page<Department> selectPageDept(int page, int limit, String hoscode, String depcode) {
        //设置排序规则
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        //设置分页参数
        //第一页 0
        Pageable pageable = PageRequest.of(page - 1, limit, sort);
        //设置条件
        Department department = new Department();
        department.setHoscode(hoscode);
        department.setDepcode(depcode);
        Example<Department> example = Example.of(department);
        //调用方法实现分页查询
        Page<Department> pageModel = departmentRepository.findAll(pageable);
        return pageModel;
    }

    @Override
    public void remove(String hoscode, String depcode) {
        //先进行查询
        Department department = departmentRepository.getDeptByHoscodeAndDepcode(hoscode, depcode);
        if (department!=null){
            departmentRepository.deleteById(department.getId());
        }
    }

    //根据医院编号查询医院科室，返回树形结构
    @Override
    public List<DepartmentVo> find(String hospCode) {
        //创建list用于封装最终返回数据
        List<DepartmentVo> result = new ArrayList<>();
        //根据hosCode查询所有科室
        Department departmentQuery  = new Department();
        departmentQuery.setHoscode(hospCode);
        Example<Department> example = Example.of(departmentQuery);
        List<Department> departmentList = departmentRepository.findAll(example);
        //使用Stream流根据大科室编号进行分组
        Map<String,List<Department>> departmentVoMap = departmentList.stream().collect(Collectors.groupingBy(Department::getBigcode));
        //遍历map集合封装数据
        for (Map.Entry<String,List<Department>> entry:departmentVoMap.entrySet()){
            //获取大科室数据
            List<Department> departments = entry.getValue();
            //获取大科室编号，name
            String bigCode = entry.getKey();
            String bigName = departments.get(0).getBigname();
            //封装大科室
            DepartmentVo departmentVoBig = new DepartmentVo();
            departmentVoBig.setDepcode(bigCode);
            departmentVoBig.setDepname(bigName);
            //封装小科室
            List<DepartmentVo> children =new ArrayList<>();
            for (Department department : departments) {
                DepartmentVo departmentVoSmall = new DepartmentVo();
                departmentVoSmall.setDepname(department.getDepname());
                departmentVoSmall.setDepcode(department.getDepcode());

                children.add(departmentVoSmall);
            }
            //将小科室信息封装到对应大科室中
            departmentVoBig.setChildren(children);
            //将大科室数据封装到返回结果中
            result.add(departmentVoBig);
        }
        return result;
    }
}










