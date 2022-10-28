package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 医院设置表 服务类
 * </p>
 *
 * @author ATOPOS
 * @since 2022-10-17
 */
public interface HospitalSetService extends IService<HospitalSet> {

    String getHospSignKey(String hoscode);

}
