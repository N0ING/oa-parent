package com.oa.process.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.common.result.Result;
import com.oa.model.process.ProcessType;
import com.oa.process.service.OaProcessTemplateService;
import com.oa.process.service.OaProcessTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 审批类型 前端控制器
 * </p>
 *
 * @author oa
 * @since 2023-03-29
 */
@Api(value = "审批类型", tags = "审批类型")
@RestController
@RequestMapping(value = "/admin/process/processType")
public class OaProcessTypeController {

    @Autowired
    private OaProcessTypeService processTypeService;

    @ApiOperation(value = "获取全部审批分类")
    @GetMapping("findAll")
    public Result findAll(){
        return Result.ok(processTypeService.list());
    }


    //@PreAuthorize("hasAuthority('bnt.processType.list')")
    @ApiOperation(value = "获取分页列表")
    @GetMapping("{page}/{limit}")
    public Result index(@PathVariable Long page,
                        @PathVariable Long limit){
        // 创建page对象,传递分页相关参数
        Page pageParam = new Page(page,limit);
        IPage<ProcessType> pageModel = processTypeService.page(pageParam);
        return Result.ok(pageModel);
    }

    //@PreAuthorize("hasAuthority('bnt.processType.list')")
    @ApiOperation(value = "获取")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id){
        ProcessType processType = processTypeService.getById(id);
        return  Result.ok(processType);
    }

    //@PreAuthorize("hasAuthority('bnt.processType.add')")
    @ApiOperation(value = "添加")
    @PostMapping("save")
    public Result save(@RequestBody ProcessType processType){

        boolean is_success = processTypeService.save(processType);
        if(is_success){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

    //@PreAuthorize("hasAuthority('bnt.processType.update')")
    @ApiOperation(value = "修改")
    @PutMapping("update")
    public  Result update(@RequestBody ProcessType processType){

        boolean is_success = processTypeService.updateById(processType);
        if(is_success){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

    // 根据id删除
    //@PreAuthorize("hasAuthority('bnt.processType.remove')")
    @ApiOperation("根据id删除")
    @DeleteMapping("remove/{id}")
    public  Result removeById(@PathVariable Long id){
        boolean is_success = processTypeService.removeById(id);
        if(is_success){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

    //@PreAuthorize("hasAuthority('bnt.processType.remove')")
    @ApiOperation("根据id删除")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList){

        boolean is_success = processTypeService.removeByIds(idList);
        if(is_success){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

}

