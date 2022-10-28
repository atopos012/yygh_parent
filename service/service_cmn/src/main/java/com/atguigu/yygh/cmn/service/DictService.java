package com.atguigu.yygh.cmn.service;


import com.atguigu.yygh.model.cmn.Dict;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 组织架构表 服务类
 * </p>
 *
 * @author ATOPOS
 * @since 2022-10-21
 */
public interface DictService extends IService<Dict> {

    List<Dict> getDataById(Long id);

    void exportDictData(HttpServletResponse response);

    void importDictData(MultipartFile file);

    //根据value值返回对应名称
    String getNameByValue(String dictCode,String value );

    List<Dict> getByDictCode(String dictCode);
}
