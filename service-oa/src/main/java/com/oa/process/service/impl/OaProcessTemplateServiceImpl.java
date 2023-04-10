package com.oa.process.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.model.process.ProcessTemplate;
import com.oa.model.process.ProcessType;
import com.oa.process.mapper.OaProcessTemplateMapper;
import com.oa.process.service.OaProcessService;
import com.oa.process.service.OaProcessTemplateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.process.service.OaProcessTypeService;
import org.activiti.engine.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 审批模板 服务实现类
 * </p>
 *
 * @author oa
 * @since 2023-03-29
 */
@Service
public class OaProcessTemplateServiceImpl extends ServiceImpl<OaProcessTemplateMapper, ProcessTemplate> implements OaProcessTemplateService {

    @Autowired
    private OaProcessTypeService ProcessTypeService;

    @Autowired
    private OaProcessService processService;


    // 分页查询审查模版 ，把审查类型对应名称查询
    @Override
    public IPage<ProcessTemplate> SelectPage(Page pageParam) {

        Page processTemplatePage = baseMapper.selectPage(pageParam, null);

        List<ProcessTemplate> processTemplateList = processTemplatePage.getRecords();

        List<Long> idList = new ArrayList<>();

        for(ProcessTemplate processTemplate:processTemplateList){
            Long processTypeId = processTemplate.getProcessTypeId();
            LambdaQueryWrapper<ProcessType> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProcessType::getId,processTypeId);
            ProcessType processType = ProcessTypeService.getOne(wrapper);
            if(processType == null){
                continue;
            }
            processTemplate.setProcessTypeName(processType.getName());
        }


        return processTemplatePage;
    }

    // 部署流程定义（发布）
    @Override
    public void publish(Long id) {
        // 修改模版状态值 1 已经发部
        ProcessTemplate processTemplate = baseMapper.selectById(id);
        processTemplate.setStatus(1);
        baseMapper.updateById(processTemplate);
       // 流程定义部署 后续完善
        if(!StringUtils.isEmpty(processTemplate.getProcessDefinitionPath())){
            processService.deployByZip(processTemplate.getProcessDefinitionPath());
        }

    }
}
