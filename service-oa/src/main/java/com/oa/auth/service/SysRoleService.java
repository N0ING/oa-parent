package com.oa.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.model.system.SysRole;
import com.oa.vo.system.AssginRoleVo;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author NO
 * @create 2023-03-20-11:54
 */

public interface SysRoleService extends IService<SysRole> {
    Map<String, Object> findRoleDataByUserId(Long userId);

    void doAssign(AssginRoleVo assginRoleVo);
}
