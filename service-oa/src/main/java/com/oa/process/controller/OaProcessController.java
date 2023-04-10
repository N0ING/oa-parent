package com.oa.process.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.common.result.Result;
import com.oa.model.process.Process;
import com.oa.process.service.OaProcessService;
import com.oa.vo.process.ProcessQueryVo;
import com.oa.vo.process.ProcessVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 审批类型 前端控制器
 * </p>
 *
 * @author oa
 * @since 2023-03-29
 */
@Api(tags = "审批流管理")
@RestController
@RequestMapping(value = "/admin/process")
@CrossOrigin
public class OaProcessController {
    @Autowired
    private OaProcessService processService;

    //@PreAuthorize("hasAuthority('bnt.process.list')")
    @ApiOperation(value = "获取分页列表")
    @GetMapping("{page}/{limit}")
    public Result index(
            @PathVariable Long page,
            @PathVariable Long limit,
            ProcessQueryVo processQueryVo){
        Page<ProcessVo> pageParam = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page,limit);
        IPage<ProcessVo> pageModel =
                processService.selectPage(pageParam,processQueryVo);

        return Result.ok(pageModel);
    }

}

