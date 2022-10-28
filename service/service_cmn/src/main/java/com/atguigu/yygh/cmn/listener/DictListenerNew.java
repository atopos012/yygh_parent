package com.atguigu.yygh.cmn.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.util.ListUtils;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;

import java.util.List;

/**
 * 不使用spring管理
 */
public class DictListenerNew extends AnalysisEventListener<DictEeVo> {

    //设置多少条提交
    private static final int BATCH_COUNT = 3;//为了测试，实际使用中可以100条
    //创建集合用于缓存
    private List<Dict> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    //创建成员变量
    private DictService dictService;

    //创建有参构造
    public DictListenerNew(DictService dictService) {
        this.dictService = dictService;
    }

    //从excel表第二行逐行读取，将每行内容封装到对象中
    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        //dictEeVo对象，每行封装的数据对象，将其添加到数据库中
        //DictEeVo ---> Dict
        Dict dict = new Dict();
        BeanUtils.copyProperties(dictEeVo, dict);

        //封装对象添加到缓存cachedDataList集合
        cachedDataList.add(dict);
        //判断读取数据条数是否达到临界值
        if (cachedDataList.size() >= BATCH_COUNT) {
            //调用方法添加
            savaData();
            //清空缓存
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    //提交缓存中数据的方法
    private void savaData() {
        dictService.saveBatch(cachedDataList);
    }

    //所有操作完成之后执行
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        //防止缓存中数据丢失
        savaData();
    }
}
