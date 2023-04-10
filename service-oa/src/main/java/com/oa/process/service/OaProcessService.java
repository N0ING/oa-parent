package com.oa.process.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.model.process.Process;
import com.oa.vo.process.ApprovalVo;
import com.oa.vo.process.ProcessFormVo;
import com.oa.vo.process.ProcessQueryVo;
import com.oa.vo.process.ProcessVo;

import java.util.Map;

/**
 * <p>
 * 审批类型 服务类
 * </p>
 *
 * @author oa
 * @since 2023-03-29
 */
public interface OaProcessService extends IService<Process> {

    // 审批管理列表
    IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo);

    // 部署流程定义
    void deployByZip(String deployPath);

    // 启动流程
    void startUp(ProcessFormVo processFormVo);

    // 查询待处理的流程
    IPage<ProcessVo> findPending(Page<Process> pageParam);

    // 获取审批详情
    Map<String, Object> show(Long id);

    // 审批
    void approve(ApprovalVo approvalVo);

    // 已处理
    IPage<ProcessVo> findProcessed(Page<ProcessVo> pageParam);

    // 已发起
    IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam);
}
