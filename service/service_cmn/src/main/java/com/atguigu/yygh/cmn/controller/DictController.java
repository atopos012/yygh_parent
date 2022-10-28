package com.atguigu.yygh.cmn.controller;


import com.alibaba.excel.EasyExcel;
import com.atguigu.yygh.cmn.listener.DictListenerNew;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * <p>
 * 组织架构表 前端控制器
 * </p>
 *
 * @author ATOPOS
 * @since 2022-10-21
 */
@Api(tags = "数据字典接口")
@RestController
//@CrossOrigin    //跨域，添加GateWay网关后会冲突
@RequestMapping("/admin/cmn/dict/")
public class DictController {
    @Autowired
    private DictService dictService;

    //查询所有省（学历，医院等级等）
    @ApiOperation(value = "根据dictCode获取下级节点")
    @GetMapping(value = "findByDictCode/{dictCode}")
    public R findByDictCode(
            @ApiParam(name = "dictCode", value = "节点编码", required = true)
            @PathVariable String dictCode) {
        List<Dict> list = dictService.getByDictCode(dictCode);
        return R.ok().data("list", list);
    }

    @ApiOperation(value = "获取数据字典名称")
    @GetMapping(value = "/getName/{parentDictCode}/{value}")
    public String getName(
            @ApiParam(name = "parentDictCode", value = "上级编码", required = true)
            @PathVariable("parentDictCode") String parentDictCode,
            @ApiParam(name = "value", value = "值", required = true)
            @PathVariable("value") String value) {
        return dictService.getNameByValue(parentDictCode, value);
    }

    @ApiOperation(value = "获取数据字典名称")
    @GetMapping(value = "/getName/{value}")
    public String getName(
            @ApiParam(name = "value", value = "值", required = true)
            @PathVariable("value") String value) {
        return dictService.getNameByValue("", value);
    }

    @ApiOperation("数据字典导入（不使用Spring管理监听器）")
    @PostMapping("importDataNew")
    public R importDataNew(MultipartFile file) {//<input type="file" name="file"/> 参数名称要与name属性值相同
        try {
            EasyExcel.read(file.getInputStream(), DictEeVo.class, new DictListenerNew(dictService)).sheet().doRead();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.ok();
    }

    @ApiOperation("数据字典导入（使用Spring管理监听器）")
    @PostMapping("importData")
    public R importData(MultipartFile file) {//<input type="file" name="file"/> 参数名称要与name属性值相同
        //获取上传文件    MultipartFile
        dictService.importDictData(file);

        return R.ok();
    }

    @ApiOperation("数据字典导出")
    @GetMapping("exportData")
    public void exportData(HttpServletResponse response) {
        dictService.exportDictData(response);
    }

    //列表接口
    //懒加载效果
    @ApiOperation("数据字典列表")
    @GetMapping("findChildData/{id}")
    public R findChildData(@PathVariable Long id) {
        List<Dict> list = dictService.getDataById(id);
        return R.ok().data("list", list);
    }
}

