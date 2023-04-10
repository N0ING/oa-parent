package com.oa.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.auth.mapper.SysRoleMapper;
import com.oa.auth.mapper.SysUserRoleMapper;
import com.oa.auth.service.SysRoleService;
import com.oa.auth.service.SysUserRoleService;
import com.oa.model.system.SysRole;
import com.oa.model.system.SysUserRole;
import com.oa.vo.system.AssginRoleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author NO
 * @create 2023-03-20-11:55
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Autowired
    private SysUserRoleService sysUserRoleService;



    @Override
    public Map<String, Object> findRoleDataByUserId(Long userId) {

        // 查询所有角色，返回List集合
        List<SysRole> allRoleList = baseMapper.selectList(null);
        // 根据用户id获取角色id
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper();
        wrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> UserRoleIdList = sysUserRoleService.list(wrapper);

//        for (SysUserRole sysUserRole:RoleIdList
//             ) {
//            Long roleId = sysUserRole.getRoleId();
//            list().add(roleId);
//        }

        List<Long> RoleIdList = UserRoleIdList.stream().map(c -> c.getRoleId()).collect(Collectors.toList());
        // 通过角色id判断角色list集合获取角色信息
        //对角色进行分类
        List<SysRole> assginRoleList = new ArrayList<>();
        for (SysRole role : allRoleList) {
            //已分配
            if(RoleIdList.contains(role.getId())) {
                assginRoleList.add(role);
            }
        }

        Map<String, Object> roleMap = new HashMap<>();
        roleMap.put("assginRoleList",assginRoleList);
        roleMap.put("allRolesList", allRoleList);
        return roleMap;

    }

    @Override
    public void doAssign(AssginRoleVo assginRoleVo) {

        // 把用户之前分配的角色删除
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, assginRoleVo.getUserId());
        sysUserRoleService.remove(wrapper);

        // 把用户新分配的角色添加
        for(Long roleId : assginRoleVo.getRoleIdList()) {
            if(StringUtils.isEmpty(roleId)) continue;
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(assginRoleVo.getUserId());
            userRole.setRoleId(roleId);
            sysUserRoleService.save(userRole);
        }
    }

//    @Autowired
//    private SysRoleMapper sysRoleMapper;
}
