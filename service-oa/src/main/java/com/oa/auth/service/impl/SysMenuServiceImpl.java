package com.oa.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.auth.helper.MenuHelper;
import com.oa.auth.mapper.SysMenuMapper;
import com.oa.auth.service.SysMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.auth.service.SysRoleMenuService;
import com.oa.auth.service.SysUserRoleService;
import com.oa.common.config.exception.OaException;
import com.oa.common.result.Result;
import com.oa.model.system.SysMenu;
import com.oa.model.system.SysRoleMenu;
import com.oa.vo.system.AssginMenuVo;
import com.oa.vo.system.MetaVo;
import com.oa.vo.system.RouterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author oa
 * @since 2023-03-24
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    @Autowired
    private SysUserRoleService sysUserRoleService;

    // 菜单列表
    @Override
    public List<SysMenu> findNodes() {

        // 查询所有菜单数据
        List<SysMenu> sysMenuList = baseMapper.selectList(null);
        // 构建树型结构
//        {
//            第一层
//                 [
//                    {
//                        第二层
//                                ...
//                    }
//                  ]
//        }
        List<SysMenu> resultList = MenuHelper.buildTree(sysMenuList);

        return resultList;
    }

    @Override
    public void removeMenuById(Long id) {
        // 判断当前菜单是否有下一层菜单
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getParentId,id);
        Integer count = baseMapper.selectCount(wrapper);
        if(count > 0){
           throw  new OaException(201, "此菜单不能删除！");
        }
        baseMapper.deleteById(id);

    }

    // 查询所有菜单和角色分配的菜单
    @Override
    public List<SysMenu> findMenuByRoleId(Long roleId) {
        // 查询所有可用菜单 - 添加条件status =1
        LambdaQueryWrapper<SysMenu> SysMenuWrapper = new LambdaQueryWrapper<>();
        SysMenuWrapper.eq(SysMenu::getStatus, 1);
        List<SysMenu> sysMenuList = baseMapper.selectList(SysMenuWrapper);

        //  角色分配的菜单
        LambdaQueryWrapper<SysRoleMenu> SysRoleMenuWrapper = new LambdaQueryWrapper<>();
        SysRoleMenuWrapper.eq(SysRoleMenu::getRoleId, roleId);
        List<SysRoleMenu> roleMenuList = sysRoleMenuService.list(SysRoleMenuWrapper);
        // 获取菜单id
        List<Long> menuIdList = new ArrayList();
        for (SysRoleMenu roleMenu: roleMenuList) {
            menuIdList.add(roleMenu.getMenuId());
        }
        // 根据菜单id，获取对应菜单对象
        sysMenuList.forEach(item->{
            if(menuIdList.contains(item.getId().longValue())){
                item.setSelect(true);
            }else {
                item.setSelect(false);
            }
        });
        // 返回规定树型结构格式菜单列表
        List<SysMenu> trees = MenuHelper.buildTree(sysMenuList);

        return trees;
    }

    // 角色分配菜单权限
    @Override
    public void doAssign(AssginMenuVo assignMenuVo) {
        // 根据角色id删除菜单角色表旧数据
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, assignMenuVo.getRoleId());
        sysRoleMenuService.remove(wrapper);
        // 从参数中获取新数据，重新分配
        List<Long> menuIdList = assignMenuVo.getMenuIdList();
        for(Long menuId :menuIdList){
            if(StringUtils.isEmpty(menuId)){
                continue;
            }
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setMenuId(menuId);
            sysRoleMenu.setRoleId(assignMenuVo.getRoleId());
            sysRoleMenuService.save(sysRoleMenu);
        }

    }

    // 根据用户id获取用户可操作菜单列表
    @Override
    public List<RouterVo> getMenuListByUserId(Long userId) {
        List<SysMenu> sysMenuList = null;
        // 如果是管理员，直接查询所有菜单列表  超级管理员admin账号id为：1
        if (userId.longValue() == 1){
            LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getStatus, 1);
            wrapper.orderByAsc(SysMenu::getSortValue);
            sysMenuList = baseMapper.selectList(wrapper);

        }else {
            // 如果不是管理员，根据userId查询对应菜单列表
            // 多表关联查询： 用户角色关系表，角色菜单关系表，菜单表
            sysMenuList = baseMapper.getMenuListByUserId(userId);
        }

        // 把查询出来的数据列表-构建成框架需要的路由结构
        // 构建树型结构
        List<SysMenu> tree = MenuHelper.buildTree(sysMenuList);
        //构建成框架需要的路由结构
        List<RouterVo> routerVoList =  this.buildRouter(tree);
        return routerVoList;
    }

    //构建成框架需要的路由结构
    private List<RouterVo> buildRouter(List<SysMenu> menus) {
        List<RouterVo> routers = new ArrayList<>();
        for (SysMenu menu : menus) {
            RouterVo router = new RouterVo();
            router.setHidden(false);
            router.setAlwaysShow(false);
            router.setPath(getRouterPath(menu));
            router.setComponent(menu.getComponent());
            router.setMeta(new MetaVo(menu.getName(), menu.getIcon()));
            List<SysMenu> children = menu.getChildren();
            //如果当前是菜单，需将按钮对应的路由加载出来，如：“角色授权”按钮对应的路由在“系统管理”下面
            if(menu.getType().intValue() == 1){
                List<SysMenu> hiddenMenuList = children.stream()
                        .filter(item -> !StringUtils.isEmpty(item.getComponent()))
                        .collect(Collectors.toList());
                for (SysMenu hiddenMenu : hiddenMenuList) {
                    RouterVo hiddenRouter = new RouterVo();
                    hiddenRouter.setHidden(true);
                    hiddenRouter.setAlwaysShow(false);
                    hiddenRouter.setPath(getRouterPath(hiddenMenu));
                    hiddenRouter.setComponent(hiddenMenu.getComponent());
                    hiddenRouter.setMeta(new MetaVo(hiddenMenu.getName(), hiddenMenu.getIcon()));
                    routers.add(hiddenRouter);
                }
            }else {
                if(!CollectionUtils.isEmpty(children)){
                    if(children.size() > 0) {
                        router.setAlwaysShow(true);
                    }
                    router.setChildren(buildRouter(children));
                }
            }
            routers.add(router);
        }
        return routers;
    }

    /**
     * 获取路由地址
     *
     * @param menu 菜单信息
     * @return 路由地址
     */
    public String getRouterPath(SysMenu menu) {
        String routerPath = "/" + menu.getPath();
        if(menu.getParentId().intValue() != 0) {
            routerPath = menu.getPath();
        }
        return routerPath;
    }

    // 根据用户id获取用户可操作按钮权限
    @Override
    public List<String> getPermsListByUserId(Long userId) {
        List<SysMenu> sysMenuList = null;
        // 如果是管理员，直接查询所有按钮权限  超级管理员admin账号id为：1
        if(userId.longValue() == 1){
            LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getStatus, 1);
            wrapper.orderByAsc(SysMenu::getSortValue);
            sysMenuList = baseMapper.selectList(wrapper);
        }else {
            // 如果不是管理员，根据userId查询对应按钮权限
            // 多表关联查询： 用户角色关系表，角色菜单关系表，菜单表
            sysMenuList = baseMapper.getMenuListByUserId(userId);
        }
        // 把查询出来的按钮权限
        List<String> permsList = sysMenuList.stream()
                .filter(item -> item.getType() == 2)
                .map(item -> item.getPerms())
                .collect(Collectors.toList());

        return permsList;
    }
}
