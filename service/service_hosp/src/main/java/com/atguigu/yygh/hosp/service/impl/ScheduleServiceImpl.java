package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScheduleServiceImpl implements ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private HospitalService hospitalService;

    @Override
    public void save(Map<String, Object> newObjectMap) {
        String jsonString = JSONObject.toJSONString(newObjectMap);
        Schedule schedule = JSONObject.parseObject(jsonString, Schedule.class);
        String hoscode = schedule.getHoscode();
        String hosScheduleId = schedule.getHosScheduleId();
        Schedule existSchedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if (existSchedule != null) {
            schedule.setId(existSchedule.getId());
            schedule.setCreateTime(existSchedule.getCreateTime());
            schedule.setUpdateTime(new Date());
            scheduleRepository.save(schedule);
        } else {
            schedule.setUpdateTime(new Date());
            schedule.setCreateTime(new Date());
            scheduleRepository.save(schedule);
        }

    }

    @Override
    public Page<Schedule> selectPageSchedule(int page, int limit, String hoscode, String depcode) {
        //设置排序规则
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        //设置分页参数
        //第一页 0
        Pageable pageable = PageRequest.of(page - 1, limit, sort);
        Schedule schedule = new Schedule();
        schedule.setHoscode(hoscode);
        schedule.setDepcode(depcode);
        Example<Schedule> example = Example.of(schedule);
        Page<Schedule> pageModel = scheduleRepository.findAll(pageable);
        return pageModel;
    }

    @Override
    public void remove(String hoscode, String hosScheduleId) {
        Schedule schedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if (schedule != null) {
            scheduleRepository.deleteById(schedule.getId());
        }
    }


    @Override
    public Map<String, Object> getRuleSchedule(Long page, Long limit,
                                               String hoscode, String depcode) {
        //根据医院编号+科室编号查询
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        //分组条件封装
        Aggregation aggregation = Aggregation.newAggregation(
            //匹配条件
            Aggregation.match(criteria),
            //分组字段
            Aggregation.group("workDate")
                    .first("workDate").as("workDate")
                    //统计号源数量
                    .count().as("docCount")
                    .sum("reservedNumber").as("reservedNumber")
                    .sum("availableNumber").as("availableNumber"),
            //排序
            Aggregation.sort(Sort.Direction.ASC,"workDate"),
            //分页
            Aggregation.skip((page-1)*limit),
            Aggregation.limit(limit)
        );
        AggregationResults<BookingScheduleRuleVo> bookingScheduleRuleVos = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = bookingScheduleRuleVos.getMappedResults();

        //分组总记录数
        Aggregation aggregationTotal = Aggregation.newAggregation(
            Aggregation.match(criteria),
            Aggregation.group("workDate")
        );
        AggregationResults<BookingScheduleRuleVo> aggregateTotal = mongoTemplate.aggregate(aggregationTotal, Schedule.class, BookingScheduleRuleVo.class);
        int total = aggregateTotal.getMappedResults().size();

        //获取日期对应星期
        for (BookingScheduleRuleVo bookingScheduleRuleVo : bookingScheduleRuleVoList) {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }
        //封装数据
        Map<String,Object> map = new HashMap<>();
        map.put("bookingScheduleRuleList",bookingScheduleRuleVoList);
        map.put("total",total);
        //封装其它基本数据
        Hospital hospital = hospitalService.getHosp(hoscode);
        String hosname = hospital.getHosname();
        Map<String,Object> baseMap =new HashMap<>();
        baseMap.put("hospname",hosname);
        map.put("baseMap",baseMap);
        return map;
    }

    @Override
    public List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate) {
        Date date = new DateTime(workDate).toDate();
        List<Schedule> scheduleList = scheduleRepository.findScheduleByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, date);
        return scheduleList;
    }

    /**
     * 根据日期获取周几数据
     *
     * @param dateTime
     * @return
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }
}
