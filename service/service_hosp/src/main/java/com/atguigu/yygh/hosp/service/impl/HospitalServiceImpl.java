package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.cmn.client.DictFeignClient;
import com.atguigu.yygh.enums.DictEnum;
import com.atguigu.yygh.hosp.repository.HospitalRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private DictFeignClient dictFeignClient;

    //添加医院数据
    @Override
    public void saveHosp(Map<String, Object> newObjectMap) {
        //newObjectMap ---> Hospital
        //json 工具实现
        //newObjectMap转换为字符串
        String jsonString = JSONObject.toJSONString(newObjectMap);
        //json字符串转换为Hospital对象
        Hospital hospital = JSONObject.parseObject(jsonString, Hospital.class);

        //判断当前医院数据是否已经添加过，若添加过进行修改，若未添加过进行添加
        //根据医院编号进行查询
        Hospital existHospital = hospitalRepository.findByHoscode(hospital.getHoscode());

        if (existHospital != null) {//已经添加过，执行修改
            //设置id值
            hospital.setId(existHospital.getId());
            hospital.setCreateTime(existHospital.getCreateTime());
            hospital.setUpdateTime(new Date());
            //调用方法添加
            hospitalRepository.save(hospital);
        } else {//执行添加
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospitalRepository.save(hospital);
        }
    }

    @Override
    public Hospital getHosp(String hoscode) {
        Hospital hospital = hospitalRepository.findByHoscode(hoscode);
        return hospital;
    }

    //医院条件分页查询
    @Override
    public Page<Hospital> selectPageHosp(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page - 1, limit, sort);
        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo, hospital);
        Example<Hospital> example = Example.of(hospital, matcher);

        Page<Hospital> pageModel = hospitalRepository.findAll(example, pageable);

        //获取查询List集合
        pageModel.getContent().stream().forEach(item->{
            //遍历list集合，得到每个Hospital对象
            this.packHospital(item);
        });

        return pageModel;
    }

    //医院状态更新
    @Override
    public void updateStatus(String id, Integer status) {
        if(status.intValue()==1||status.intValue()==0){
           Hospital hospital = hospitalRepository.findById(id).get();
           hospital.setStatus(status);
           hospital.setUpdateTime(new Date());
           hospitalRepository.save(hospital);
        }
    }

    @Override
    public Map<String,Object>show(String id) {
        Map<String,Object> map = new HashMap<>();
        Hospital hospital = this.packHospital(hospitalRepository.findById(id).get());
        //医院基本信息（包含医院等级）
        map.put("hospital",hospital);
        //单独处理更直观
        map.put("bookingRule", hospital.getBookingRule());
        //不需要重复返回
        hospital.setBookingRule(null);
        return map;
    }

    @Override
    public List<Hospital> findByHosname(String hosname) {
        return hospitalRepository.findHospitalByHosnameLike(hosname);
    }

    @Override
    public Map<String, Object> item(String hoscode) {
        Map<String,Object> map = new HashMap<>();
        Hospital hospital = this.packHospital(this.hospitalRepository.findByHoscode(hoscode));
        map.put("hospital",hospital);
        map.put("bookingRule", hospital.getBookingRule());
        //不需要重复返回
        hospital.setBookingRule(null);
        return map;
    }

    //获取每个对象编号，远程调用根据编号获取名称，将名称封装到Hospital对象的map中
    private Hospital packHospital(Hospital hospital) {
        //获取每个对象编号
        String hostype = hospital.getHostype();

        String provinceCode = hospital.getProvinceCode();
        String cityCode = hospital.getCityCode();
        String districtCode = hospital.getDistrictCode();

        //远程调用,从数据字典模块中获取参数
        String provinceString = dictFeignClient.getName(provinceCode);
        String cityString = dictFeignClient.getName(cityCode);
        String districtString = dictFeignClient.getName(districtCode);
        //获取医院等级
        String hostypeString = dictFeignClient.getName(DictEnum.HOSTYPE.getDictCode(), hostype);
        //数据封装
        hospital.getParam().put("hostypeString", hostypeString);
        hospital.getParam().put("fullAddress", provinceString + cityString + districtString + hospital.getAddress());
        return hospital;
    }
}






