package com.atguigu.yygh.cmn;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExcelListener extends AnalysisEventListener<Student> {
    //创建list集合封装最终的数据
    List<Student> list = new ArrayList<Student>();
    //一行一行去读取excle内容
    //从第二行进行读取，默认第一行为表头
    @Override
    public void invoke(Student user, AnalysisContext analysisContext) {
        System.out.println("***"+user);
        list.add(user);
    }
    //读取excel表头信息
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        System.out.println("表头信息："+headMap);
    }
    //读取完成后执行
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
    }
}