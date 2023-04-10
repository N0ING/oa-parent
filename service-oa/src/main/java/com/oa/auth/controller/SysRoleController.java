package com.oa.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mysql.cj.log.Log;
import com.oa.auth.service.SysRoleService;
import com.oa.auth.service.SysUserRoleService;
import com.oa.common.result.Result;
import com.oa.model.system.SysRole;
import com.oa.model.system.SysUserRole;
import com.oa.vo.system.AssginRoleVo;
import com.oa.vo.system.SysRoleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author NO
 * @create 2023-03-20-12:07
 */
@Api(tags ="角色管理接口")
@RestController
@RequestMapping("/admin/system/sysRole")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;


    // 根据用户获取角色数据
    @ApiOperation("根据用户获取角色数据")
    @GetMapping("/toAssign/{userId}")
    public Result toAssign(@PathVariable Long userId){

        Map<String,Object> map = sysRoleService.findRoleDataByUserId(userId);

        return Result.ok(map);
    }

    // 根据用户分配角色
    @ApiOperation("根据用户分配角色")
    @PostMapping("/doAssign")
    public Result doAssign(@RequestBody AssginRoleVo assginRoleVo){

        sysRoleService.doAssign(assginRoleVo);
        return Result.ok();
    }


    // 查询所有角色
    @ApiOperation(value = "查询所有角色")
    @GetMapping("findAll")
    public Result findAll(){
        List<SysRole> sysRoleList = sysRoleService.list();
        return Result.ok(sysRoleList);
    }

    // 条件分页查询
    // page 当前页 limit 每页记录数 SysRoleQueryVo 条件对象
    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation(value = "条件分页查询")
    @GetMapping("{page}/{limit}")
    public Result pageQueryRole(@PathVariable Long page,
                                @PathVariable Long limit,
                                SysRoleQueryVo sysRoleQueryVo) {
        // 创建page对象,传递分页相关参数
        Page pageParam = new Page(page,limit);
        // 封装分页条件，判断条件是否为空
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        String roleName = sysRoleQueryVo.getRoleName();
        if(!StringUtils.isEmpty(roleName)){
            wrapper.like(SysRole::getRoleName,roleName);
        }

        IPage<SysRole> pageModel = sysRoleService.page(pageParam, wrapper);

        return Result.ok(pageModel);
    }

    // 添加角色
    @PreAuthorize("hasAuthority('bnt.sysRole.add')")
    @ApiOperation("添加角色")
    @PostMapping("save")
    public Result saveRole(@RequestBody SysRole sysRole){
        boolean is_success = sysRoleService.save(sysRole);
        if(is_success){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

    // 根据id查询
    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation("根据id查询")
    @GetMapping("get/{id}")
    public Result getById(@PathVariable Long id){
        SysRole sysRole = sysRoleService.getById(id);
        return Result.ok(sysRole);
    }

    // 修改角色-最终修改
    @PreAuthorize("hasAuthority('bnt.sysRole.update')")
    @ApiOperation("修改角色")
    @PutMapping("update")
    public Result updateRole(@RequestBody SysRole sysRole){
        boolean is_success = sysRoleService.updateById(sysRole);
        if(is_success){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

    // 删除角色-根据id删除
    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation("删除角色-根据id删除")
    @DeleteMapping("remove/{id}")
    public Result removeRole(@PathVariable Long id){
        boolean is_success = sysRoleService.removeById(id);
        if(is_success){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

    // 删除角色-批量删除
    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation("删除角色-批量删除")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList){
        boolean is_success = sysRoleService.removeByIds(idList);
        if(is_success){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

}
