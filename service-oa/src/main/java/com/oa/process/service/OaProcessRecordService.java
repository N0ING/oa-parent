package com.oa.process.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.model.process.ProcessRecord;

/**
 * <p>
 * 审批记录 服务类
 * </p>
 *
 * @author oa
 * @since 2023-04-02
 */
public interface OaProcessRecordService extends IService<ProcessRecord> {
    void record(Long processId,Integer status,String description);
}
