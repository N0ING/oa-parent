package com.oa.process.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.auth.service.SysUserService;
import com.oa.common.result.Result;
import com.oa.model.process.Process;
import com.oa.model.process.ProcessRecord;
import com.oa.model.process.ProcessTemplate;
import com.oa.model.system.SysUser;
import com.oa.process.mapper.OaProcessMapper;
import com.oa.process.service.OaProcessRecordService;
import com.oa.process.service.OaProcessService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.process.service.OaProcessTemplateService;
import com.oa.security.custom.LoginUserInfoHelper;
import com.oa.vo.process.ApprovalVo;
import com.oa.vo.process.ProcessFormVo;
import com.oa.vo.process.ProcessQueryVo;
import com.oa.vo.process.ProcessVo;
import com.oa.wechat.service.MessageService;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.zip.ZipInputStream;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author oa
 * @since 2023-03-29
 */
@Service
public class OaProcessServiceImpl extends ServiceImpl<OaProcessMapper,Process> implements OaProcessService {

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private OaProcessTemplateService processTemplateService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private OaProcessRecordService processRecordService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private MessageService messageService;


    // 审批管理列表
    @Override
    public IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo) {
        IPage<ProcessVo> pageModel = baseMapper.selectPage(pageParam, processQueryVo);
        return pageModel;
    }

    // 部署流程定义
    @Override
    public void deployByZip(String deployPath) {
        InputStream inputStream =
                this.getClass().getClassLoader().getResourceAsStream(deployPath);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        Deployment deployment = repositoryService
                .createDeployment()
                .addZipInputStream(zipInputStream).deploy();
        System.out.println(deployment.getId()+deployment.getName());
    }

    // 启动流程
    @Override
    public void startUp(ProcessFormVo processFormVo) {
        // 根据当前用户id查询用户信息
        SysUser sysUser = sysUserService.getById(LoginUserInfoHelper.getUserId());
        // 根据审批模板id查询模版信息
        ProcessTemplate processTemplate = processTemplateService.getById(processFormVo.getProcessTemplateId());
        // 保存提交审批模版信息到业务表 oa_process
        Process process = new Process();
        // 将processFormVo复制到Process中
        BeanUtils.copyProperties(processFormVo,process);
        process.setStatus(1);
        String workNo = System.currentTimeMillis() + "";
        process.setProcessCode(workNo);
        process.setUserId(LoginUserInfoHelper.getUserId());
        process.setFormValues(processFormVo.getFormValues());
        process.setTitle(sysUser.getName() + "发起" + processTemplate.getName() + "申请");
        baseMapper.insert(process);
        // 流程定义的key
        String processDefinitionKey = processTemplate.getProcessDefinitionKey();

        System.out.println("==========================================="+processDefinitionKey);
        // 业务key processId
        String businessKey = String.valueOf(process.getId());
        // 流程参数 将form表单中的json数据转化为map
        String formValues = processFormVo.getFormValues();
        JSONObject jsonObject = JSON.parseObject(formValues);
        JSONObject formData = jsonObject.getJSONObject("formData");
        Map<String,Object> map = new HashMap<>();
        for(Map.Entry<String,Object> entry:formData.entrySet()){
            map.put(entry.getKey(), entry.getValue());
        }
        Map<String,Object> variables = new HashMap<>();
        variables.put("data", map);
        // 启动流程实例 - RuntimeService
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey,businessKey,variables);
        // 查询下一个审批人
        List<Task> taskList = this.getCurrentTaskList(processInstance.getId());
        List<String> nameList = new ArrayList<>();
        for (Task task:taskList){
            String assigneeName = task.getAssignee();
            SysUser user = sysUserService.getUserByUserName(assigneeName);
            String name = user.getName();
            nameList.add(name);
            //  推送消息
            messageService.pushPendingMessage(process.getId(), user.getId(), task.getId());
        }
        process.setProcessInstanceId(processInstance.getId());
        process.setDescription("等待"+ StringUtils.join(nameList.toArray(), ","+"审批"));
        // 业务和流程进行最终的关联
        baseMapper.updateById(process);
        // 记录操作审批记录
        processRecordService.record(process.getId(), 1, "发起审批");
    }

    // 查询待处理的流程
    @Override
    public IPage<ProcessVo> findPending(Page<Process> pageParam) {
        // 封装查询条件，根据当前登录用户名称
        TaskQuery query = taskService.createTaskQuery().
                taskAssignee(LoginUserInfoHelper.getUsername()).orderByTaskCreateTime().desc();
        // 调用方法分页查询，返回list待办任务集合
        // 参数 开始位置  每页记录数
        int begin = (int) ((pageParam.getCurrent() - 1) * pageParam.getSize());
        int size = (int) ( pageParam.getSize());
        List<Task> taskList = query.listPage(begin, size);
        long totalCount = query.count();

        // 封装list集合数据到list<processVo>中
        List<ProcessVo> processVoList = new ArrayList<>();
        for (Task task : taskList) {
            String processInstanceId = task.getProcessInstanceId();
            ProcessInstance processInstance = runtimeService.
                    createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            String businessKey = processInstance.getBusinessKey();
            if(businessKey == null){
                continue;
            }
            Long processId = Long.parseLong(businessKey);
            Process process = baseMapper.selectById(processId);
            ProcessVo processVo = new ProcessVo();
            BeanUtils.copyProperties(process,processVo);
            processVo.setTaskId(task.getId());
            processVoList.add(processVo);
        }
        // 封装返回Ipage对象
        IPage<ProcessVo> page = new Page<>(pageParam.getCurrent(),pageParam.getSize(),totalCount);
        page.setRecords(processVoList);
        return page;
    }
    // 获取审批详情
    @Override
    public Map<String, Object> show(Long id) {
        // 根据流程id查询流程信息process
        Process process = baseMapper.selectById(id);
        // 根据流程id查询流程记录信息
        LambdaQueryWrapper<ProcessRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessRecord::getProcessId, id);
        List<ProcessRecord> processRecordList = processRecordService.list(wrapper);
        // 根据模版id查询模版信息
        Long processTemplateId = process.getProcessTemplateId();
        ProcessTemplate processTemplate = processTemplateService.getById(processTemplateId);
        // 判断当前用户是否有权限可以审批
        boolean isApprove = false;
        List<Task> taskList = this.getCurrentTaskList(process.getProcessInstanceId());
        for (Task task:taskList) {
            // 判断任务审批人是否是当前用户
            if(task.getAssignee().equals(LoginUserInfoHelper.getUsername())){
                isApprove = true;
            }
        }
        // 查询数据封装到map集合
        Map<String, Object> map = new HashMap<>();
        map.put("process", process);
        map.put("processRecordList", processRecordList);
        map.put("processTemplate", processTemplate);
        map.put("isApprove", isApprove);
        return map;
    }
    // 审批
    @Override
    public void approve(ApprovalVo approvalVo) {
        Map<String, Object> variables1 = taskService.getVariables(approvalVo.getTaskId());
        for (Map.Entry<String, Object> entry : variables1.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
        }
        String taskId = approvalVo.getTaskId();
        if (approvalVo.getStatus() == 1) {
            // 已通过
            Map<String, Object> variables = new HashMap<String, Object>();
            taskService.complete(taskId, variables);
        } else {
            // 驳回
            this.endTask(taskId);
        }
        // 记录审批相关过程信息到process_record表中
        String description = approvalVo.getStatus().intValue() == 1 ? "已通过" : "驳回";
        processRecordService.record(approvalVo.getProcessId(), approvalVo.getStatus(), description);

        // 查询下一个审批人，更新流程信息
        Process process = baseMapper.selectById(approvalVo.getProcessId());
        List<Task> taskList = this.getCurrentTaskList(process.getProcessInstanceId());
        if(!CollectionUtils.isEmpty(taskList)){
            List<String> assignList = new ArrayList<>();
            for(Task task:taskList){
                String assignee = task.getAssignee();
                SysUser sysUser = sysUserService.getUserByUserName(assignee);
                assignList.add(sysUser.getName());

                // TODO 公众号消息推送
            }
            process.setDescription("等待" + StringUtils.join(assignList.toArray(), ",") + "审批");
            process.setStatus(1);
        }else{
            if(approvalVo.getStatus().intValue() == 1) {
                process.setDescription("审批完成（同意）");
                process.setStatus(2);
            } else {
                process.setDescription("审批完成（拒绝）");
                process.setStatus(-1);
            }
        }
        baseMapper.updateById(process);
    }

    // 已处理
    @Override
    public IPage<ProcessVo> findProcessed(Page<ProcessVo> pageParam) {
        // 封装查询条件
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery().
                taskAssignee(LoginUserInfoHelper.getUsername()).finished().orderByTaskCreateTime().desc();
        // 调用方法条件分页查询，返回list集合
        int begin = (int) ((pageParam.getCurrent() - 1) * pageParam.getSize());
        int size = (int) ( pageParam.getSize());
        List<HistoricTaskInstance> historicTaskList = query.listPage(begin, size);
        long total = query.count();
        // 遍历list集合，封装list<processVO>
        List<ProcessVo> processVoList = new ArrayList<>();
        for (HistoricTaskInstance item : historicTaskList) {
            String processInstanceId = item.getProcessInstanceId();
            LambdaQueryWrapper<Process> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Process::getProcessInstanceId, processInstanceId);
            Process process = baseMapper.selectOne(wrapper);
            ProcessVo processVo = new ProcessVo();
            BeanUtils.copyProperties(process, processVo);
            processVoList.add(processVo);
        }
        // IPage封装分页查询所有数据返回
        IPage<ProcessVo> pageModel = new Page<>(pageParam.getCurrent(),pageParam.getSize(),total);
        pageModel.setRecords(processVoList);
        return pageModel;
    }

    // 已发起
    @Override
    public IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam) {
        ProcessQueryVo processQueryVo = new ProcessQueryVo();
        processQueryVo.setUserId(LoginUserInfoHelper.getUserId());
        IPage<ProcessVo> pageModel = baseMapper.selectPage(pageParam, processQueryVo);
        return pageModel;
    }

    // 结束流程
    private void endTask(String taskId) {
        //  当前任务
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        List endEventList = bpmnModel.getMainProcess().findFlowElementsOfType(EndEvent.class);
        // 并行任务可能为null
        if(CollectionUtils.isEmpty(endEventList)) {
            return;
        }
        FlowNode endFlowNode = (FlowNode) endEventList.get(0);
        FlowNode currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());

        //  临时保存当前活动的原始方向
        List originalSequenceFlowList = new ArrayList<>();
        originalSequenceFlowList.addAll(currentFlowNode.getOutgoingFlows());
        //  清理活动方向
        currentFlowNode.getOutgoingFlows().clear();

        //  建立新方向
        SequenceFlow newSequenceFlow = new SequenceFlow();
        newSequenceFlow.setId("newSequenceFlowId");
        newSequenceFlow.setSourceFlowElement(currentFlowNode);
        newSequenceFlow.setTargetFlowElement(endFlowNode);
        List newSequenceFlowList = new ArrayList<>();
        newSequenceFlowList.add(newSequenceFlow);
        //  当前节点指向新的方向
        currentFlowNode.setOutgoingFlows(newSequenceFlowList);

        //  完成当前任务
        taskService.complete(task.getId());
    }

    private List<Task> getCurrentTaskList(String id) {
        List<Task> list = taskService.createTaskQuery().processInstanceId(id).list();
        return list;
    }

}
