package com.oa.process.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.model.process.ProcessTemplate;
import com.oa.model.process.ProcessType;

import java.util.List;

/**
 * <p>
 * 审批类型 服务类
 * </p>
 *
 * @author oa
 * @since 2023-03-29
 */
public interface OaProcessTypeService extends IService<ProcessType> {

    // 获取全部审批分类及模板
    List<ProcessType> findProcessType();

}
