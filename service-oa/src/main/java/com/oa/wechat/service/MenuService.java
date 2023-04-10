package com.oa.wechat.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.model.wechat.Menu;
import com.oa.vo.wechat.MenuVo;

import java.util.List;

/**
 * <p>
 * 菜单 服务类
 * </p>
 *
 * @author oa
 * @since 2023-04-05
 */
public interface MenuService extends IService<Menu> {

    // 获取全部菜单
    List<MenuVo> findMenuInfo();

    // 同步菜单
    void syncMenu();

    // 删除菜单
    void removeMenu();
}
