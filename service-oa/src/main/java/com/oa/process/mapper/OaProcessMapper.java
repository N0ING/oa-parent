package com.oa.process.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.model.process.Process;
import com.oa.vo.process.ProcessQueryVo;
import com.oa.vo.process.ProcessVo;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 审批类型 Mapper 接口
 * </p>
 *
 * @author oa
 * @since 2023-03-29
 */
public interface OaProcessMapper extends BaseMapper<Process> {

    // 审批管理列表
    public IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam,@Param("vo") ProcessQueryVo processQueryVo);

}
