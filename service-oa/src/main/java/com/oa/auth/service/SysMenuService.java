package com.oa.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.model.system.SysMenu;
import com.oa.vo.system.AssginMenuVo;
import com.oa.vo.system.RouterVo;

import java.util.List;

/**
 * <p>
 * 菜单表 服务类
 * </p>
 *
 * @author oa
 * @since 2023-03-24
 */
public interface SysMenuService extends IService<SysMenu> {

    // 菜单列表
    List<SysMenu> findNodes();

    // 删除菜单
    void removeMenuById(Long id);

    // 查询所有菜单和角色分配的菜单
    List<SysMenu> findMenuByRoleId(Long roleId);

    // 角色分配菜单权限
    void doAssign(AssginMenuVo assignMenuVo);

    // 根据用户id获取用户可操作菜单列表
    List<RouterVo> getMenuListByUserId(Long userId);

    // 根据用户id获取用户可操作按钮权限
    List<String> getPermsListByUserId(Long userId);
}
