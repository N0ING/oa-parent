package com.oa.auth.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.model.system.SysUser;

import java.util.Map;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author oa
 * @since 2023-03-23
 */
public interface SysUserService extends IService<SysUser> {

    void updateStatus(Long id, Integer status);

    // 根据用户名查询用户
    SysUser getUserByUserName(String username);

    // 获取当前用户信息
    Map<String, Object> getCurrentUser();
}
