package com.atguigu.yygh.cmn.service.impl;


import com.alibaba.excel.EasyExcel;
import com.atguigu.yygh.cmn.listener.DictListener;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 组织架构表 服务实现类
 * </p>
 *
 * @author ATOPOS
 * @since 2022-10-21
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    //    @Autowired
//    private DictMapper dictMapper;
    //service 调用mapper不需要自己注入，使用baseMapper就可以调用

    @Autowired
    private DictListener dictListener;

    //数据字典导出
    @Override
    public void exportDictData(HttpServletResponse response) {

        try {
            //设置下载信息 头
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = null;
            fileName = URLEncoder.encode("数据字典", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

            //查询数据字典所有数据
            List<Dict> dictList = baseMapper.selectList(null);

            //List<Dict> --->List<DictEeVo>
            List<DictEeVo> dictEeVoList = new ArrayList<>();
            for (Dict dict : dictList) {
//                Long id = dict.getId();
//                String name = dict.getName();
//                Long parentId = dict.getParentId();
//                DictEeVo dictEeVo =new DictEeVo();
//                dictEeVo.setId(id);
//                dictEeVo.setName(name);
//                dictEeVo.setParentId(parentId);
//                dictEeVoList.add(dictEeVo);

                //上面代码优化
                DictEeVo dictEeVo = new DictEeVo();
                BeanUtils.copyProperties(dict, dictEeVo);

                dictEeVoList.add(dictEeVo);
            }
            //使用EasyExcel写操作
            EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("数据字典").doWrite(dictEeVoList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //数据字典导入
    @CacheEvict(value = "dict", allEntries = true)
    @Override
    public void importDictData(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), DictEeVo.class, dictListener)
                    .sheet()
                    .doRead();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //根据value值返回对应名称
    @Override
    public String getNameByValue(String dictCode, String value) {
        //判断value是否唯一
        if (StringUtils.isEmpty(dictCode)) {//value唯一
            //直接查询
//            LambdaQueryWrapper<Dict> wrapper1 = new LambdaQueryWrapper<>();
//            wrapper1.eq(Dict::getValue,value);
            QueryWrapper<Dict> wrapper = new QueryWrapper<>();
            wrapper.eq("value", value);
            Dict dict = baseMapper.selectOne(wrapper);
            if (dict != null) {
                return dict.getName();
            }

        } else {//value不唯一
            //根据dictcode查询上层id
            Dict dictParent = this.getDictByDictCode(dictCode);
            if (dictParent != null) {
                //获取上层id值
                Long pid = dictParent.getId();
                QueryWrapper<Dict> wrapper = new QueryWrapper<>();
                wrapper.eq("parent_id", pid);
                wrapper.eq("value", value);
                Dict dict = baseMapper.selectOne(wrapper);
                return dict.getName();
            }
        }
        return null;
    }

    //查询所有省（学历，医院等级等）
    @Override
    public List<Dict> getByDictCode(String dictCode) {
        Dict dict = this.getDictByDictCode(dictCode);
        if (dict != null) {
            QueryWrapper<Dict> wrapper = new QueryWrapper<>();
            wrapper.eq("parent_id",dict.getId());
            List<Dict> dictList = baseMapper.selectList(wrapper);
            return dictList;
        }
        return null;
    }

    //根据dictcode查询上层id
    private Dict getDictByDictCode(String dictCode) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_code", dictCode);
        Dict dict = baseMapper.selectOne(wrapper);
        return dict;
    }

    //获取数据字典列表
    @Cacheable("dict")
    @Override
    public List<Dict> getDataById(Long id) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", id);
        List<Dict> dictList = baseMapper.selectList(wrapper);
        //查询数据库获取的数据往往不能满足前端需求，需要将查询数据再进一步封装
        //Dict有属性hasChildren，如果该属性为true，有下层数据，为false没有下层数据
        //遍历dictList 得到每个dict对象，判断每个dict对象是否有下层数据hasChildren=true
        for (Dict dict : dictList) {
            //判断dict是否有下层数据parent_id = dict的id
            Long dictId = dict.getId();
            boolean flag = isChildData(dictId);
            //返回值，设置hasChildren
            dict.setHasChildren(flag);
        }
        return dictList;
    }

    //判断是否有下层数据
    private boolean isChildData(Long dictId) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", dictId);
        Integer count = baseMapper.selectCount(wrapper);
        return count > 0;
    }
}
