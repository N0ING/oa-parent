package com.oa.process.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.model.process.ProcessTemplate;
import com.oa.model.process.ProcessType;

/**
 * <p>
 * 审批模板 服务类
 * </p>
 *
 * @author oa
 * @since 2023-03-29
 */
public interface OaProcessTemplateService extends IService<ProcessTemplate> {

    // 分页查询审查模版 ，把审查类型对应名称查询
    IPage<ProcessTemplate> SelectPage(Page pageParam);

    // 部署流程定义（发布）
    void publish(Long id);
}
