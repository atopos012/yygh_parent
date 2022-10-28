package com.atguigu.yygh.cmn;

import com.alibaba.excel.EasyExcel;

import java.util.ArrayList;
import java.util.List;

public class TestWrite {

    public static void main(String[] args) {
        String fileName = "E:\\test.xlsx";
        EasyExcel.write(fileName, Student.class).sheet("写操作").doWrite(data());
    }
    //循环设置要添加的数据，最终封装到list集合中
    private static List<Student> data() {
        List<Student> list = new ArrayList<Student>();
        for (int i = 0; i < 10; i++) {
            Student data = new Student();
            data.setSno(i);
            data.setSname("张三"+i);
            list.add(data);
        }
        return list;
    }
}
