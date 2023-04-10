package com.oa.auth.helper;

import com.oa.model.system.SysMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * @author NO
 * @create 2023-03-24-9:14
 */
public class MenuHelper {

    // 构建树型结构
    public static List<SysMenu> buildTree(List<SysMenu> sysMenuList) {

        // 使用递归方法建菜单
        List<SysMenu> trees = new ArrayList<>();
        for (SysMenu sysMenu: sysMenuList){
            if(sysMenu.getParentId().longValue() == 0){
                trees.add(findChildren(sysMenu,sysMenuList));
            }
        }
        return trees;
    }

    // 递归查找子节点
    private static SysMenu findChildren(SysMenu sysMenu, List<SysMenu> sysMenuList) {

        sysMenu.setChildren(new ArrayList<SysMenu>());

        for (SysMenu item :sysMenuList){
            if(sysMenu.getId().longValue() == item.getParentId().longValue()){
                if (sysMenu.getChildren() == null) {
                    sysMenu.setChildren(new ArrayList<>());
                }
                sysMenu.getChildren().add(findChildren(item,sysMenuList));
            }
        }
        return  sysMenu;
    }
}
