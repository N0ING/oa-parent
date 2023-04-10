package com.oa.wechat.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.oa.auth.service.SysUserService;
import com.oa.model.process.Process;
import com.oa.model.process.ProcessTemplate;
import com.oa.model.system.SysUser;
import com.oa.process.service.OaProcessService;
import com.oa.process.service.OaProcessTemplateService;
import com.oa.security.custom.LoginUserInfoHelper;
import com.oa.wechat.service.MessageService;
import io.netty.util.internal.StringUtil;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.WxMpTemplateMsgService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author NO
 * @create 2023-04-07-10:12
 */
@Service
public class MessageServiceImpl implements MessageService {
    @Resource
    private WxMpService wxMpService;

    @Resource
    private OaProcessService processService;

    @Resource
    private OaProcessTemplateService processTemplateService;

    @Resource
    private SysUserService sysUserService;

    @Override
    public void pushPendingMessage(Long processId, Long userId, String taskId) {
        // 流程信息
        Process process = processService.getById(processId);
        // 推送人的信息
        SysUser user = sysUserService.getById(userId);
        // 模版信息
        ProcessTemplate processTemplate = processTemplateService.getById(process.getProcessTemplateId());
        // 提交人的信息
        SysUser submitUser = sysUserService.getById(process.getUserId());

        String openId = user.getOpenId();
        if(StringUtils.isEmpty(openId)){
            // TODO 为了测试使用
            openId = "omwf25izKON9dktgoy0dogqvnGhk";
        }

        // 设置消息发送消息
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder().toUser(openId)
                .templateId("\tFWoWH24bpD4nlSiG9E2z4eSxCoCFgMkm7eFAbOW3Ysg")
                .url("http://oaoaoa.free.svipss.top/#/show/" + processId + "/" + taskId)
                .build();
        // 设置模版参数的值
        JSONObject jsonObject = JSON.parseObject(process.getFormValues());
        JSONObject formShowData = jsonObject.getJSONObject("formShowData");
        StringBuffer content = new StringBuffer();
        for (Map.Entry entry : formShowData.entrySet()) {
            content.append(entry.getKey()).append("：").append(entry.getValue()).append("\n ");
        }
        templateMessage.addData(new WxMpTemplateData("first", submitUser.getName()+"提交了"+processTemplate.getName()+"审批申请，请注意查看。", "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword1", process.getProcessCode(), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword2", new DateTime(process.getCreateTime()).toString("yyyy-MM-dd HH:mm:ss"), "#272727"));
        templateMessage.addData(new WxMpTemplateData("content", content.toString(), "#272727"));
        String msg = null;
        try {
            msg = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
        System.out.println("推送消息返回："+ msg);

    }

    @Override
    public void pushProcessedMessage(Long processId, Long userId, Integer status) {
        Process process = processService.getById(processId);
        ProcessTemplate processTemplate = processTemplateService.getById(process.getProcessTemplateId());
        SysUser sysUser = sysUserService.getById(userId);
        SysUser currentSysUser = sysUserService.getById(LoginUserInfoHelper.getUserId());
        String openid = sysUser.getOpenId();
        if(StringUtils.isEmpty(openid)) {
            openid = "omwf25izKON9dktgoy0dogqvnGhk";
        }
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser(openid)//要推送的用户openid
                .templateId("e1TbcwF_hQw19-L_GIU3U-j3bWUTgSO5xLtyYg0HJHg")//模板id
                .url("http://oaoaoa.free.svipss.top/#/show/"+processId+"/0")//点击模板消息要访问的网址
                .build();
        JSONObject jsonObject = JSON.parseObject(process.getFormValues());
        JSONObject formShowData = jsonObject.getJSONObject("formShowData");
        StringBuffer content = new StringBuffer();
        for (Map.Entry entry : formShowData.entrySet()) {
            content.append(entry.getKey()).append("：").append(entry.getValue()).append("\n ");
        }
        templateMessage.addData(new WxMpTemplateData("first", "你发起的"+processTemplate.getName()+"审批申请已经被处理了，请注意查看。", "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword1", process.getProcessCode(), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword2", new DateTime(process.getCreateTime()).toString("yyyy-MM-dd HH:mm:ss"), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword3", currentSysUser.getName(), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword4", status == 1 ? "审批通过" : "审批拒绝", status == 1 ? "#009966" : "#FF0033"));
        templateMessage.addData(new WxMpTemplateData("content", content.toString(), "#272727"));
        String msg = null;
        try {
            msg = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
        } catch (WxErrorException e) {
            e.printStackTrace();
        }

        System.out.println("推送消息返回：{}"+ msg);
    }
}
