package com.oa.auth.controller;


import com.oa.auth.service.SysMenuService;
import com.oa.common.result.Result;
import com.oa.model.system.SysMenu;
import com.oa.vo.system.AssginMenuVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 菜单表 前端控制器
 * </p>
 *
 * @author oa
 * @since 2023-03-24
 */
@Api(tags = "菜单管理接口")
@RestController
@RequestMapping("/admin/system/sysMenu")
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    // 查询所有菜单和角色分配的菜单
    @ApiOperation("查询所有菜单和角色分配的菜单")
    @GetMapping("toAssign/{roleId}")
    public Result toAssign(@PathVariable Long roleId){
        List<SysMenu> sysMenuList = sysMenuService.findMenuByRoleId(roleId);
        return  Result.ok(sysMenuList);
    }
    // 角色分配菜单权限
    @ApiOperation(value = "角色分配菜单权限")
    @PostMapping("/doAssign")
    public Result doAssign(@RequestBody AssginMenuVo assignMenuVo) {

        sysMenuService.doAssign(assignMenuVo);
        return Result.ok();
    }


    // 菜单列表
    @ApiOperation("菜单列表")
    @GetMapping("findNodes")
    public Result findNodes(){

       List<SysMenu> sysMenus   = sysMenuService.findNodes();

        return  Result.ok(sysMenus);
    }


    @ApiOperation(value = "新增菜单")
    @PostMapping("save")
    public Result save(@RequestBody SysMenu permission) {
        sysMenuService.save(permission);
        return Result.ok();
    }

    @ApiOperation(value = "修改菜单")
    @PutMapping("update")
    public Result updateById(@RequestBody SysMenu permission) {
        sysMenuService.updateById(permission);
        return Result.ok();
    }

    @ApiOperation(value = "删除菜单")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        sysMenuService.removeMenuById(id);
        return Result.ok();
    }

}

